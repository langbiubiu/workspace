package ipaneltv.toolkit.http;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.http.HttpObjectification.IPBouquet;
import ipaneltv.toolkit.http.HttpObjectification.IPEITInfo;
import ipaneltv.toolkit.http.HttpObjectification.IPFrequencyInfo;
import ipaneltv.toolkit.http.HttpObjectification.IPNetwork;
import ipaneltv.toolkit.http.HttpObjectification.IPSDTService;
import ipaneltv.toolkit.http.HttpObjectification.IPTransportStream;
import ipaneltv.toolkit.http.HttpObjectification.SdtService;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.format.Time;
 
public class SaxHandler extends DefaultHandler {
	private static final String TAG = SaxHandler.class.getSimpleName();
	public IPNetwork ipNetwork = null;
	IPNetwork.TransportStream  ts = null;
	IPNetwork.TransportStream.Channel channel = null;
	IPNetwork.TransportStream.Channel.Stream stream = null;
	IPNetwork.TransportStream.Channel.Stream.ECM ecm = null;
	
	public IPBouquet ipBouquet = null;
	IPBouquet.Bouquet bouquet = null;
	IPBouquet.Bouquet.Channel bChannel = null;
	
	public IPEITInfo ipEitInfo = null;
	IPEITInfo.Channel eChannel = null;
	IPEITInfo.Channel.Event event = null;
	Time t = new Time();
	
	long start,end;
	
    /* 此方法有三个参数
       arg0是传回来的字符数组，其包含元素内容
       arg1和arg2分别是数组的开始位置和结束位置 */
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
 
    /* arg0是名称空间
       arg1是包含名称空间的标签，如果没有名称空间，则为空
       arg2是不包含名称空间的标签 */
    @Override
    public void endElement(String arg0, String arg1, String arg2)
            throws SAXException {
        super.endElement(arg0, arg1, arg2);
    } 
   
	/*arg0是名称空间
      arg1是包含名称空间的标签，如果没有名称空间，则为空
      arg2是不包含名称空间的标签
      arg3很明显是属性的集合 */
    @Override
    public void startElement(String arg0, String arg1, String arg2,
            Attributes arg3) throws SAXException {
    	IPanelLog.e(TAG,"arg2 = "+arg2);
    	if("network".equalsIgnoreCase(arg2)){
    		ipNetwork = createNetwork();
    	}else if("ts".equalsIgnoreCase(arg2)){
			ts = createTS();
			ts.ipFreq = new IPFrequencyInfo();
		}else if("ch".equalsIgnoreCase(arg2)){
	
			if (ts != null && bouquet == null && ipEitInfo == null) {
				channel = ts.addChannel();
				channel.sdtService = new IPSDTService();
			} 	
			
			if(bouquet != null && ipEitInfo == null){
				bChannel = bouquet.addChannel();
				bChannel.ipts = new IPTransportStream();
				bChannel.service = new SdtService();
			}
			IPanelLog.e(TAG,"ipEitInfo = "+ipEitInfo);
			if(ipEitInfo != null){
				IPanelLog.e(TAG,"ipEitInfo addChannel");
				eChannel = ipEitInfo.addChannel();
				eChannel.ipts = new IPTransportStream();
				eChannel.service = new SdtService();
			}
		}else if("es".equalsIgnoreCase(arg2)){
			stream = channel.addStream();
		}else if("ecm".equalsIgnoreCase(arg2)){
			ecm = stream.addECM();
		}else if("BAT".equalsIgnoreCase(arg2)){
			ipBouquet = new IPBouquet();
		}else if("bou".equalsIgnoreCase(arg2)){
			bouquet = ipBouquet.addBouquet();
		}else if("EIT".equalsIgnoreCase(arg2)){
			if (ipEitInfo == null) {
				ipEitInfo = new IPEITInfo();
			}
		}else if("e".equalsIgnoreCase(arg2)){
			if(eChannel != null){
				event = eChannel.addEvent();
			}
		}else{
			IPanelLog.e(TAG,"error!!!");
		}
				
    	if (arg3 != null) {
            for (int i = 0; i < arg3.getLength(); i++) {
                 // getQName()是获取属性名称，
            	String qname = arg3.getQName(i);
            	String value = arg3.getValue(i);
            	IPanelLog.i(TAG,"qname = "+qname);
            	IPanelLog.i(TAG,"value = "+value);
                if("name".equalsIgnoreCase(qname)){
                	if("network".equalsIgnoreCase(arg2)){
                		ipNetwork.name = value;
                	}else if("ch".equalsIgnoreCase(arg2)){
                		channel.name = value;
                	}else if("bou".equalsIgnoreCase(arg2)){
                		bouquet.name = value;
                	}else if("e".equalsIgnoreCase(arg2)){
                		if (event != null) {
							event.name = value;
						}
                	}
                }else if("delivery".equalsIgnoreCase(qname)){
                	ipNetwork.delivery = value;
                }else if("nid".equalsIgnoreCase(qname)){
                	ipNetwork.nid = SaxHandlerUtil.parseString2Int(value);
                }else if("version".equalsIgnoreCase(qname)){
                	if("network".equalsIgnoreCase(arg2)){
                		ipNetwork.version = SaxHandlerUtil.parseString2Int(value);
                	}else if("BAT".equalsIgnoreCase(arg2)){
                		ipBouquet.version = SaxHandlerUtil.parseString2Int(value);
                	}else if("EIT".equalsIgnoreCase(arg2)){
                		ipEitInfo.version = SaxHandlerUtil.parseString2Int(value);
                	}
                }else if("onid".equalsIgnoreCase(qname)){    		
                	ts.onid = SaxHandlerUtil.parseString2Int(value);
                }else if("freq".equalsIgnoreCase(qname)){    		
                	ts.ipFreq.freq = SaxHandlerUtil.parseString2Long(value);
                }else if("mod".equalsIgnoreCase(qname)){    		
                	ts.ipFreq.mod = value;
                }else if("rate".equalsIgnoreCase(qname)){    		
                	ts.ipFreq.rate = SaxHandlerUtil.parseString2Long(value);
                }else if("tsid".equalsIgnoreCase(qname)){    
                	if("ts".equalsIgnoreCase(arg2)){
                		ts.tsid = SaxHandlerUtil.parseString2Int(value);
                	}else if("ch".equalsIgnoreCase(arg2)){
                		if (bChannel != null && eChannel == null) {
							bChannel.ipts.tsid = SaxHandlerUtil.parseString2Int(value);
						}
                		
                		if(eChannel != null){
                			eChannel.ipts.tsid = SaxHandlerUtil.parseString2Int(value);
                		}
                	}
                }else if("number".equalsIgnoreCase(qname)){    		
                	channel.number = SaxHandlerUtil.parseString2Int(value);
                }else if("sid".equalsIgnoreCase(qname)){   
                	if(channel != null && bChannel == null && eChannel == null){
                		channel.sdtService.sid = SaxHandlerUtil.parseString2Int(value);
                	}
                	if(bChannel != null && eChannel == null){
                		bChannel.service.sid = SaxHandlerUtil.parseString2Int(value);
                	}  
                	
                	if(eChannel != null){
                		eChannel.service.sid = SaxHandlerUtil.parseString2Int(value);
                	}               	
                }else if("type".equalsIgnoreCase(qname)){    		
                	if("ch".equalsIgnoreCase(arg2)){
                		channel.sdtService.type = SaxHandlerUtil.parseString2Int(value);                		
                	}else if("es".equalsIgnoreCase(arg2)){
                		stream.type = SaxHandlerUtil.parseString2Int(value);
                	}
                }else if("tstf".equalsIgnoreCase(qname)){    		
                	channel.tstf = SaxHandlerUtil.parseString2Int(value);
                }else if("pid".equalsIgnoreCase(qname)){    		
                	if("es".equalsIgnoreCase(arg2)){
                		stream.pid = SaxHandlerUtil.parseString2Int(value);
                	}else if("ecm".equalsIgnoreCase(arg2)){
                		ecm.pid = SaxHandlerUtil.parseString2Int(value);
                	}
                }else if("caids".equalsIgnoreCase(qname)){   
                	ecm.caids = SaxHandlerUtil.parseString2Int(value);
                }else if("id".equalsIgnoreCase(qname)){    		
                	bouquet.id = SaxHandlerUtil.parseString2Int(value);
                }else if("capid".equalsIgnoreCase(qname)){    		
                	ecm.capid = value;
                }else if("desc".equalsIgnoreCase(qname)){    		
                	if (event != null) {
                		event.desc = value;
					}
                }else if("st".equalsIgnoreCase(qname)){ 
                	if ("e".equalsIgnoreCase(arg2)) {
						value = SaxHandlerUtil.chang2UTCFormate(value);
						if (event != null) {
							t.parse3339(value);
							event.st = t.toMillis(true);
							
						}
					}
                	
                	if("EIT".equalsIgnoreCase(arg2)){
                		t.parse3339(SaxHandlerUtil.chang2UTCFormate(value));
                		ipEitInfo.st = t.toMillis(true);
                	}
                	
                }else if("et".equalsIgnoreCase(qname)){  
                	if ("e".equalsIgnoreCase(arg2)) {
						value = SaxHandlerUtil.chang2UTCFormate(value);
						if (event != null) {
							t.parse3339(value);
							event.et = t.toMillis(true);
						}
					}
                	
                	if("EIT".equalsIgnoreCase(arg2)){
                		t.parse3339(SaxHandlerUtil.chang2UTCFormate(value));
                		ipEitInfo.et = t.toMillis(true);
                	}
                }else if("channelId".equalsIgnoreCase(qname)){  
                	channel.channelId = value;  
                }else if("PCR_PID".equalsIgnoreCase(qname)){  
                	channel.pcrpid = SaxHandlerUtil.parseString2Int(value);  
                }else{
                	IPanelLog.i(TAG,"UPDATE!!!");
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

    public IPNetwork.TransportStream getTransportStream(){
    	return ts;
    }
    
    public IPNetwork createNetwork(){
    	return new IPNetwork();
    }
    
    public IPNetwork.TransportStream createTS(){
    	return ipNetwork.addTransportStream();
    }
}