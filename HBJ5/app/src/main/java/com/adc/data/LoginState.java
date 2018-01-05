package com.adc.data;

public final class LoginState {
	private static LoginState ins = new LoginState();

	private String serverURL;
	private String cityId;
	private String userId;
	private String state;
	private String csiteId;
	private String ui_type;
	private int noise_or_pm10;
	
	
	public String getUi_type() {
		return ui_type;
	}

	public void setUi_type(String ui_type) {
		this.ui_type = ui_type;
	}

	public int getNoise_or_pm10() {
		return noise_or_pm10;
	}

	public void setNoise_or_pm10(int noise_or_pm10) {
		this.noise_or_pm10 = noise_or_pm10;
	}

	public static LoginState getIns() {
		return ins;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getCsiteId() {
		return csiteId;
	}

	public void setCsiteId(String csiteId) {
		this.csiteId = csiteId;
	}


	
}
