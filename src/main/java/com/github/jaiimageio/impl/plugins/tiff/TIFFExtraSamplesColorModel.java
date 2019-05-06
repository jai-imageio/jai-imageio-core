package com.github.jaiimageio.impl.plugins.tiff;

import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.awt.image.DataBuffer;

public class TIFFExtraSamplesColorModel extends ComponentColorModel {

    private final int numComponents;

    private final int componentSize;

    public TIFFExtraSamplesColorModel(ColorSpace colorSpace,
                                      int[] bits,
                                      boolean hasAlpha,
                                      boolean isAlphaPremultiplied,
                                      int transparency,
                                      int transferType,
                                      int extraComponents) {
        super(colorSpace, bits, hasAlpha, isAlphaPremultiplied, transparency, transferType);
        this.numComponents = colorSpace.getNumComponents() + (hasAlpha ? 1 : 0) + extraComponents;
        this.componentSize = DataBuffer.getDataTypeSize(transferType);
    }

    @Override
    public int getNumComponents() {
        return numComponents;
    }

    @Override
    public int getComponentSize(int componentIdx) {
        return componentSize;
    }

    @Override
    public boolean isCompatibleSampleModel(SampleModel sm) {
        // Must have the same number of components
        return (sm instanceof ComponentSampleModel) && numComponents == sm.getNumBands() && transferType == sm.getTransferType();
    }

    @Override
    public WritableRaster getAlphaRaster(WritableRaster raster) {
        if (!hasAlpha()) {
            return null;
        }

        int x = raster.getMinX();
        int y = raster.getMinY();
        int[] band = new int[] {getAlphaComponent()};

        return raster.createWritableChild(x, y, raster.getWidth(), raster.getHeight(), x, y, band);
    }

    private int getAlphaComponent() {
        return super.getNumComponents() - 1;
    }
}
