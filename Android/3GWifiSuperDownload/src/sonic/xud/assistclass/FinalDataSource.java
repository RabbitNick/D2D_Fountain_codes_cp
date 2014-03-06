package sonic.xud.assistclass;

public class FinalDataSource {

	private static final String DATASERVERIP = "218.193.131.2";
	private static final String SPONSORIP = "192.168.43.1";
	private static final int CONTROLPORT = 8000;
	private static final int DATAPORT = 8001;
	private static int SPONSORPORT = 0x1043;
	
	public static String getDataserverip() {
		return DATASERVERIP;
	}
	public static int getControlport() {
		return CONTROLPORT;
	}
	public static int getDataport() {
		return DATAPORT;
	}
	public static String getSponsorip() {
		return SPONSORIP;
	}
	public static int getSPONSORPORT() {
		return SPONSORPORT;
	}
}
