package ipaneltv.toolkit.http;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.http.HttpObjectification.AllIPEITInfo;
import ipaneltv.toolkit.http.HttpObjectification.IPChannelAppoint;
import ipaneltv.toolkit.http.HttpObjectification.IPEITInfoAppoint;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.format.Time;
 
public class EITSaxHandler extends DefaultHandler {
	
	public AllIPEITInfo allEitInfo = new AllIPEITInfo();
	
	public IPEITInfoAppoint aipEitInfo = null; // ���ڽ���  ĳһ��Ƶ����Eit����
	IPChannelAppoint aChannel = null;
	IPChannelAppoint.AEvent aevent = null;
	Time t = new Time();
	
	long start,end;
	
    /* �˷�������������
       arg0�Ǵ��������ַ����飬�����Ԫ������
       arg1��arg2�ֱ�������Ŀ�ʼλ�úͽ���λ�� */
    @Override
    public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
        super.characters(arg0, arg1, arg2);
    }
 
    @Override
    public void endDocument() throws SAXException {
    	end = System.currentTimeMillis();
    	IPanelLog.i("begin:end-->time", System.currentTimeMillis()+"-->end");
    	IPanelLog.i("begin:end-->time", (end-start)/1000+"-->total");
        super.endDocument();
    }
 
    /* arg0�����ƿռ�
       arg1�ǰ������ƿռ�ı�ǩ�����û�����ƿռ䣬��Ϊ��
       arg2�ǲ��������ƿռ�ı�ǩ */
    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        super.endElement(arg0, arg1, arg2);
    } 
   
	/*arg0�����ƿռ�
      arg1�ǰ������ƿռ�ı�ǩ�����û�����ƿռ䣬��Ϊ��
      arg2�ǲ��������ƿռ�ı�ǩ
      arg3�����������Եļ��� */
    @Override
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
    	
    	if("ch".equalsIgnoreCase(arg2)){
			if(aipEitInfo != null){
				aChannel = aipEitInfo.addChannel();
			}		
		}else if("EIT".equalsIgnoreCase(arg2)){
			aipEitInfo = allEitInfo.addIPEITInfo();
		
		}else if("e".equalsIgnoreCase(arg2)){
			if(aChannel != null){
				aevent = aChannel.addEvent();
			}
		}else{
		}
				
    	if (arg3 != null) {
            for (int i = 0; i < arg3.getLength(); i++) {
                 // getQName()�ǻ�ȡ�������ƣ�
            	String qname = arg3.getQName(i);
            	String value = arg3.getValue(i);
                if("name".equalsIgnoreCase(qname)){
            		if(aevent != null){
            			aevent.name = value;
            		}
                }else if("version".equalsIgnoreCase(qname)){                	
                	aipEitInfo.version = SaxHandlerUtil.parseString2Int(value);
                }else if("desc".equalsIgnoreCase(qname)){    		
                	if(aevent != null){
            			aevent.desc = value;
            		}
                }else if("st".equalsIgnoreCase(qname)){ 
                	IPanelLog.d("-----------eit", "lvby-->="+value);
                	long time = 0;;
                	if ("e".equalsIgnoreCase(arg2)) {
						value = SaxHandlerUtil.chang2UTCFormate(value);
						time = 0;						
						if (aevent != null) {
							t.parse3339(value);
							time = t.toMillis(true);
							aevent.st = time;
						}
					}
					IPanelLog.d("-----------eit", "starttime="+time);
                	
                }else if("et".equalsIgnoreCase(qname)){  
                	IPanelLog.d("-----------eit", "et lvby-->="+value);
                	if ("e".equalsIgnoreCase(arg2)) {
						value = SaxHandlerUtil.chang2UTCFormate(value);
						
						if (aevent != null) {
							t.parse3339(value);
							aevent.et = t.toMillis(true);
						}
					}
                }else{
                }
            }
        }    	
        super.startElement(arg0, arg1, arg2, arg3);
    }
    
    @Override
	public void startDocument() throws SAXException {
    	start = System.currentTimeMillis();
    	IPanelLog.i("begin:end-->time", System.currentTimeMillis()+"-->start");
		super.startDocument();
	}
}