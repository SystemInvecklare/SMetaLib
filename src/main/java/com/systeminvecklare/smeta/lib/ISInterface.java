package com.systeminvecklare.smeta.lib;

import java.util.List;

public interface ISInterface extends ISType {
	List<ISInterface> getExtendedInterfaces();
}
