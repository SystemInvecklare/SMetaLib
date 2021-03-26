package com.systeminvecklare.smeta.lib;

import java.util.List;

public interface ISClass extends ISType {
	List<ISInterface> getImplementedInterfaces();
}
