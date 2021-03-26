package com.systeminvecklare.smeta.lib;


public interface ISFieldConstruction {
	void initialValue(SCode expression);
	ISFieldConstruction makeFinal();
	ISFieldConstruction makePrivate();
}
