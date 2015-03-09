package com.terracom.qrpttbeta.db;


import com.terracom.jumble.model.Server;

public class PublicServer extends Server {

<<<<<<< HEAD
    private String mCA;
    private String mContinentCode;
    private String mCountry;
    private String mCountryCode;
    private String mRegion;
    private String mUrl;

    public PublicServer(String name, String ca, String continentCode, String country, String countryCode, String ip, Integer port, String region, String url) {
        super(-1, name, ip, port, "", "");
        mCA = ca;
        mContinentCode = continentCode;
        mCountry = country;
        mCountryCode = countryCode;
        mRegion = region;
        mUrl = url;
    }

    public String getCountry() {
        return mCountry;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public String getUrl() {
        return mUrl;
    }
=======
	private String mCA;
	private String mContinentCode;
	private String mCountry;
	private String mCountryCode;
	private String mRegion;
	private String mUrl;
	
	public PublicServer(String name, String ca, String continentCode, String country, String countryCode, String ip, Integer port, String region, String url) {
        super(-1, name, ip, port, "", "");
		mCA = ca;
		mContinentCode = continentCode;
		mCountry = country;
		mCountryCode = countryCode;
		mRegion = region;
		mUrl = url;
	}

	public String getCountry() {
		return mCountry;
	}

	public String getCountryCode() {
		return mCountryCode;
	}

	public String getUrl() {
		return mUrl;
	}
>>>>>>> 07bc5cde7e6dce7050a44aecffed1740735184c0
}
