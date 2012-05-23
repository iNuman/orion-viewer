package universe.constellation.orion.viewer.djvu;

/*
 * Orion Viewer is a pdf and djvu viewer for android devices
 *
 * Copyright (C) 2011-2012  Michael Bogdanov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import universe.constellation.orion.viewer.Common;
import universe.constellation.orion.viewer.DocumentWrapper;
import universe.constellation.orion.viewer.PageInfo;

import java.util.Date;

/**
 * User: mike
 * Date: 22.11.11
 * Time: 10:42
 */
public class DjvuDocument implements DocumentWrapper {

    static {
		System.loadLibrary("djvu");
	}

    private int pageCount;

    private int lastPage = -1;

    public DjvuDocument(String fileName) {
        openDocument(fileName);
    }

    public boolean openDocument(String fileName) {
        pageCount = openFile(fileName);
        return true;
    }

    public int getPageCount() {
        return pageCount;
    }

    public PageInfo getPageInfo(int pageNum) {
        PageInfo info = new PageInfo();
        getPageInfo(pageNum, info);
        return info;
    }

    public int[] renderPage(int pageNumber, double zoom, int w, int h, int left, int top, int right, int bottom) {
        gotoPage(pageNumber);
        return drawPage((float) zoom, right - left, bottom - top, left, top, right - left, bottom - top);
    }

    public void destroy() {
        destroying();
    }

    private synchronized void gotoPage(int page) {
        if(lastPage != page) {
            Common.d("Changing page...");
            Date date = new Date();
            if (page > pageCount-1)
                page = pageCount-1;
            else if (page < 0)
                page = 0;
            gotoPageInternal(page);

            Date date2 = new Date();

            lastPage = page;
            Common.d("Page changing takes " + page + " = " + 0.001 * (date2.getTime() - date.getTime()));
        }
	}

    private static synchronized native int openFile(String filename);
	private static synchronized  native void gotoPageInternal(int localActionPageNum);
	private static synchronized  native int getPageInfo(int pageNum, PageInfo info);

	public static synchronized  native int [] drawPage(float zoom, int pageW, int pageH,
			int patchX, int patchY,
			int patchW, int patchH);
	public static synchronized  native void destroying();

    public String getTitle() {
        return null;
    }

    public native void setContrast(int contrast);
}
