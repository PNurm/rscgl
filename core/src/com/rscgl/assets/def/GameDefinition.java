package com.rscgl.assets.def;

import java.nio.ByteBuffer;

public abstract class GameDefinition {

    public abstract ByteBuffer pack();
    public abstract <T extends GameDefinition> T unpack(ByteBuffer buffer);

}
