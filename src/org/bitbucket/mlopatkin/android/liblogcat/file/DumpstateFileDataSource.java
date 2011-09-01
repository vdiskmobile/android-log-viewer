/*
 * Copyright 2011 Mikhail Lopatkin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.mlopatkin.android.liblogcat.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.bitbucket.mlopatkin.android.liblogcat.DataSource;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordDataSourceListener;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecordStream;
import org.bitbucket.mlopatkin.android.liblogcat.PidToProcessConverter;
import org.bitbucket.mlopatkin.android.liblogcat.ProcessListParser;
import org.bitbucket.mlopatkin.android.liblogcat.LogRecord.Buffer;
import org.bitbucket.mlopatkin.android.logviewer.Configuration;

public class DumpstateFileDataSource implements DataSource {
    private static final Logger logger = Logger.getLogger(DumpstateFileDataSource.class);

    private List<LogRecord> source = new ArrayList<LogRecord>();
    private PidToProcessConverter converter;
    private LogRecordDataSourceListener listener;

    public DumpstateFileDataSource(File file) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(file));
            readLog(in, LogRecord.Buffer.MAIN);
            readLog(in, LogRecord.Buffer.EVENTS);
            readLog(in, LogRecord.Buffer.RADIO);
            readProcessesList(in);
            in.close();
        } catch (IOException e) {
            logger.error("Unexpected IO exception", e);
        }
        Collections.sort(source, sortByTimeAscComparator);
    }

    DumpstateFileDataSource(BufferedReader in) throws IOException {
        readLog(in, LogRecord.Buffer.MAIN);
        readLog(in, LogRecord.Buffer.EVENTS);
        readLog(in, LogRecord.Buffer.RADIO);
        readProcessesList(in);
        Collections.sort(source, sortByTimeAscComparator);
    }

    private void readLog(BufferedReader in, LogRecord.Buffer buffer) throws IOException {
        String bufferName = Configuration.dump.bufferHeader(buffer);
        if (bufferName == null) {
            logger.warn("This kind of log isn't supported for dumpstate files:" + buffer);
            return;
        }
        scanForLogBegin(in, bufferName);
        LogRecordStream stream = new DumpstateRecordStream(in);
        LogRecord record = stream.next(buffer);
        while (record != null) {
            source.add(record);
            record = stream.next(buffer);
        }
    }

    private void scanForLogBegin(BufferedReader in, String bufferName) throws IOException {
        bufferName = bufferName.toUpperCase();
        scanForSectionBegin(in, bufferName + " LOG");
    }

    private void readProcessesList(BufferedReader in) throws IOException {
        final String PS_END = "[ps:";
        scanForSectionBegin(in, "PROCESSES (ps -P)");
        String line = in.readLine();
        if (!ProcessListParser.isProcessListHeader(line)) {
            return;
        }
        converter = new PidToProcessConverter();
        line = in.readLine();
        while (line != null && !line.startsWith(PS_END)) {
            Matcher m = ProcessListParser.parseProcessListLine(line);
            if (m.matches()) {
                int pid = ProcessListParser.getPid(m);
                String name = ProcessListParser.getProcessName(m);
                converter.put(pid, name);
            }
            line = in.readLine();
        }

    }

    private void scanForSectionBegin(BufferedReader in, String sectionHeader) throws IOException {
        String sectionBegin = "------ " + sectionHeader;
        String line = in.readLine();
        while (line != null && !line.startsWith(sectionBegin)) {
            line = in.readLine();
        }
    }

    private static class DumpstateRecordStream extends LogRecordStream {

        public DumpstateRecordStream(BufferedReader in) {
            super(in);
        }

        private static final String LOG_END = "[logcat:";

        @Override
        protected boolean isLogEnd(String line) {
            return super.isLogEnd(line) || line.startsWith(LOG_END);
        }
    }

    private static Comparator<LogRecord> sortByTimeAscComparator = new Comparator<LogRecord>() {

        public int compare(LogRecord o1, LogRecord o2) {
            return o1.getTime().compareTo(o2.getTime());
        }

    };

    @Override
    public void close() {
    }

    @Override
    public PidToProcessConverter getPidToProcessConverter() {
        return converter;
    }

    @Override
    public void setLogRecordListener(LogRecordDataSourceListener listener) {
        for (LogRecord record : source) {
            listener.onNewRecord(record, false);
        }
        this.listener = listener;
    }

    @Override
    public EnumSet<Buffer> getAvailableBuffers() {
        return EnumSet.of(Buffer.MAIN, Buffer.RADIO, Buffer.EVENTS);
    }

    @Override
    public void reset() {
        setLogRecordListener(listener);
    }
}