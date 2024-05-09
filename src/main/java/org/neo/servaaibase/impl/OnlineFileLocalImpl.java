package org.neo.servaaibase.impl;

import org.neo.servaaibase.ifc.OnlineFileSystemIFC;

public class OnlineFileLocalImpl implements OnlineFileSystemIFC {
    private String mountPoint;
    private OnlineFileLocalImpl() {
    }

    private OnlineFileLocalImpl(String inputMountPoint) {
        mountPoint = inputMountPoint;
    }

    public static OnlineFileSystemIFC getInstance(String inputMountPoint) {
        return new OnlineFileLocalImpl(inputMountPoint);
    }

    @Override 
    public String getMountPoint() {
        return mountPoint; 
    }
}
