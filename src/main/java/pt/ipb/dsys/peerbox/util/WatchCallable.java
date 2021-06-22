package pt.ipb.dsys.peerbox.util;

import pt.ipb.dsys.peerbox.common.PeerFile;

import java.util.concurrent.Callable;

public class WatchCallable implements Callable<Void> {

	PeerFile pf;

	public WatchCallable(PeerFile pf) {
		this.pf = pf;
	}

	@Override
	public Void call() throws Exception {

		WatchFolder wf = new WatchFolder(pf);
		wf.watchFolder();

		return null;
	}

}