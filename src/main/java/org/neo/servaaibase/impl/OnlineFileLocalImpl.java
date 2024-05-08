package org.neo.servaaibase.impl;

import org.neo.servaaibase.ifc.OnlineFileSystem;

public class OnlineFileLocalImpl implements OnlineFileSystem {
    private String mountPoint;
    private OnlineFileLocalImpl() {
    }

    private OnlineFileLocalImpl(String inputMountPoint) {
        mountPoint = inputMountPoint;
    }

    public static OnlineFileSystem getInstance(String inputMountPoint) {
        return new OnlineFileLocalImpl(inputMountPoint);
    }

    @Override 
    public String getMountPoint() {
        return mountPoint; 
    }
}
