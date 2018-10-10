/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.cybergarage.http.netty;

import org.cybergarage.http.netty.HttpRange.RangeIndex;
import org.jboss.netty.channel.FileRegion;
import org.jboss.netty.logging.InternalLogger;
import org.jboss.netty.logging.InternalLoggerFactory;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

public class RangeFileRegion implements FileRegion {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RangeFileRegion.class);

    private final FileChannel file;
    private final HttpRange range;
    private final long fileLength;
    private final boolean releaseAfterTransfer;

    public RangeFileRegion(FileChannel file, HttpRange position, long count) {
        this(file, position, count, false);
    }

    public RangeFileRegion(FileChannel file, HttpRange position, long count, boolean releaseAfterTransfer) {
        this.file = file;
        this.range = position;
        this.fileLength = count;
        this.releaseAfterTransfer = releaseAfterTransfer;
    }

    public long getPosition() {
        return range.getFirstRangeStart(fileLength);
    }

    public long getCount() {
        return range.getTotalCount(fileLength);
    }

    public boolean releaseAfterTransfer() {
        return releaseAfterTransfer;
    }

    public long transferTo(WritableByteChannel target, long position) throws IOException {
        long count = this.fileLength - position;
        if (count < 0 || position < 0) {
            throw new IllegalArgumentException(
                    "position out of range: " + position +
                    " (expected: 0 - " + (this.fileLength - 1) + ')');
        }
        if (count == 0) {
            return 0L;
        }
        long total = 0;
        long relativePos = 0;
        for(RangeIndex ri : range.getRanges()){
        	long start = ri.getStart(fileLength);
        	long end = ri.getEnd(fileLength);
        	long length = end - start;
        	relativePos += length;
        	if(position > relativePos)
        		continue;
        	if(position > relativePos - length){
        		length = relativePos - position;
        		total += file.transferTo(end - length, length, target);
        	}else
        		total += file.transferTo(start, length, target);
        }
        return total;
    }

    public void releaseExternalResources() {
        try {
            file.close();
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to close a file.", e);
            }
        }
    }
}
