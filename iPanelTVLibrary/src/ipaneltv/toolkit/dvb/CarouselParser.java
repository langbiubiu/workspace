package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Natives;

import java.io.IOException;

public class CarouselParser extends Natives {

	final String TAG = "CarouselParser";
	final int PARSE_DATA_CAROUSEL = 0;
	final int PARSE_OBJECT_CAROUSEL = 0;

	CarouselTookit tookit = null;

	public CarouselParser() {
		tookit = new CarouselTookit();
	}

	/**
	 * DSMCC协议下DC的DII数据解析
	 * 
	 * @param diiFileName
	 *            DII section的文件路径
	 * 
	 *            解析结果可以通过getModule的方法获取。
	 * */
	public void loadDcDII(String diiFileName) throws IOException {

		int readpos = 0, filesize = 0, rlen = 0;
		int mlen = 4096;
		int meta = Natives.calloc(1, mlen);
		int section = Natives.calloc(1, mlen);
		filesize = Natives.flength(diiFileName);

		int d = Natives.open(diiFileName, FO_RDWR);
		IPanelLog.i(TAG, "loadDcDII filename =" + diiFileName + ",fd:" + d);
		@SuppressWarnings("unused")
		int ret = (int) Natives.llseek(d, 0, Natives.SEEK_SET);
		while (readpos < filesize) {
			rlen = Natives.read(d, section, mlen);
			IPanelLog.i(TAG, "loadDcDII rlen:" + rlen);
			if (rlen > 0) {
				nParseDiiSection(section, rlen, meta, mlen, 0);
				readpos += rlen;
				byte b[] = new byte[mlen];
				njmemcpy(b, 0, meta, mlen);
				tookit.analyticDIIJson(new String(b));// 多个section
			} else
				break;
		}
		Natives.close(d);
		Natives.free(meta);
		Natives.free(section);
		IPanelLog.i(TAG, "loadDcDII end:" + readpos);
	}
	
//	/**
//	 * DSMCC协议下DC的DII数据解析
//	 * 
//	 * @param diiFileName
//	 *            DII section的文件路径
//	 * 
//	 *            解析结果可以通过getModule的方法获取。
//	 * */
//	public void loadDcDII(FileDescriptor diiFileName) throws IOException {
//
//		int readpos = 0, filesize = 0, rlen = 0;
//		int mlen = 4096;
//		int meta = Natives.calloc(1, mlen);
//		int section = Natives.calloc(1, mlen);
//		filesize = Natives.flength(diiFileName);
//
//		int d = Natives.open(diiFileName, FO_RDWR);
//		IPanelLog.i(TAG, "loadDcDII filename =" + diiFileName + ",fd:" + d);
//		int ret = (int) Natives.llseek(d, 0, Natives.SEEK_SET);
//		while (readpos < filesize) {
//			rlen = Natives.read(d, section, mlen);
//			IPanelLog.i(TAG, "loadDcDII rlen:" + rlen);
//			if (rlen > 0) {
//				nParseDiiSection(section, rlen, meta, mlen, 0);
//				readpos += rlen;
//				byte b[] = new byte[mlen];
//				njmemcpy(b, 0, meta, mlen);
//				tookit.analyticDIIJson(new String(b));// 多个section
//			} else
//				break;
//		}
//		Natives.close(d);
//		Natives.free(meta);
//		Natives.free(section);
//		IPanelLog.i(TAG, "loadDcDII end:" + readpos);
//	}

	/**
	 * DSMCC协议下DC的Module数据解析
	 * 
	 * @param srcFileName
	 *            module的section数据文件路径
	 * @param dstFileName
	 *            module解析后文件存放路径
	 * */
	public void loadDcModule(String srcFileName, String dstFileName) {
		int d = Natives.open(srcFileName, Natives.FO_RDWR);
		int readpos = 0, filesize = 0, rlen = 0;
		int waddr = 0;
		int mlen = 4096;
		int section = Natives.calloc(1, mlen);
		filesize = Natives.flength(srcFileName);
		IPanelLog.i(TAG, "loadDcModule dstFileName:" + dstFileName);
		while (readpos < filesize) {
			rlen = Natives.read(d, section, mlen);
			if (waddr == 0) {
				waddr = Natives.open(dstFileName, Natives.FO_RDWR);
			}
			nParseDcModuleSection(section, rlen, waddr);
			readpos += rlen;
			IPanelLog.i(TAG, "loadDcModule readpos:" + readpos);
		}
		if (waddr != 0)
			Natives.close(waddr);
		Natives.free(section);
		Natives.close(d);
		IPanelLog.i(TAG, "loadDcModule end:" + readpos);

	}

	/**
	 * DSMCC协议下DC的Module获取
	 * 
	 * @param mid
	 *            Module_id
	 * @param srcFileName
	 *            module的section数据文件路径
	 * @param dstFileName
	 *            module解析后文件存放路径
	 * */
	public DataCarouselModule getDcModuleInfo(int moduleId) {// --TODO
		return tookit.getModuleInfo(moduleId);
	}

	/**
	 * 获取DSMCC协议下DC的Module个数
	 */
	public int getDcModuleSize() {
		if (tookit.dcModules != null)
			return tookit.dcModules.size();
		return 0;
	}

	native int nParseDiiSection(int section, int len, int meta, int mlen, int type);

	native int nParseDcModuleSection(int section, int len, int fd);

	native int nParseOcModuleSection(int section, int len, int mpos);

}
