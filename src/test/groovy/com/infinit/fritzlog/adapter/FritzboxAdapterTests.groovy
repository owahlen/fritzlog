package com.infinit.fritzlog.adapter

import org.junit.Test

class FritzboxAdapterTests {

	@Test
	void testComputeResponse() {
		FritzBoxAdapter adapter = new FritzBoxAdapter(host: "192.168.2.1", password: "test")
		String response = adapter.metaClass.invokeMethod(adapter, 'computeResponse', 'ae08039e')
		assert "ae08039e-bfa7d5c168dbb8b8c41a9bf2cd02cd75" == response
	}
}
