package com.ruanyun.campus.teacher.api;

import java.io.IOException;

public interface RequestListener {
	void onComplete(String response);

	void onIOException(IOException e);

	void onError(CampusException e);
}
