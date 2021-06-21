package pt.ipb.dsys.peerbox.util;

import java.util.concurrent.Callable;

public class WatchCallable implements Callable<Void> {

	@Override
	public Void call() throws Exception {

		WatchFolder wf = new WatchFolder();
		wf.run();

		return null;
	}

}