#!/bin/sh

convert misto_rgb.tiff -define tiff:fill-order=lsb -endian lsb -compress LZW -strip -colorspace rgb ../lsb_misto_rgb.tiff
identify -verbose ../lsb_misto_rgb.tiff | grep Endianess

convert misto_rgb.tiff -define tiff:fill-order=msb -endian msb -compress LZW -strip -colorspace rgb ../msb_misto_rgb.tiff
identify -verbose ../msb_misto_rgb.tiff | grep Endianess

convert misto_gray.tiff -define tiff:fill-order=lsb -endian lsb -compress LZW -strip -colorspace gray ../lsb_misto_gray.tiff
identify -verbose ../lsb_misto_gray.tiff | grep Endianess

convert misto_gray.tiff -define tiff:fill-order=msb -endian msb -compress LZW -strip -colorspace gray ../msb_misto_gray.tiff
identify -verbose ../msb_misto_gray.tiff | grep Endianess

convert gradiente_rgb.tiff -define tiff:fill-order=lsb -endian lsb -compress LZW -strip -colorspace rgb ../lsb_gradiente_rgb.tiff
identify -verbose ../lsb_gradiente_rgb.tiff | grep Endianess

convert gradiente_rgb.tiff -define tiff:fill-order=msb -endian msb -compress LZW -strip -colorspace rgb ../msb_gradiente_rgb.tiff
identify -verbose ../msb_gradiente_rgb.tiff | grep Endianess

convert gradiente_gray.tiff -define tiff:fill-order=lsb -endian lsb -compress LZW -strip -colorspace gray ../lsb_gradiente_gray.tiff
identify -verbose ../lsb_gradiente_gray.tiff | grep Endianess

convert gradiente_gray.tiff -define tiff:fill-order=msb -endian msb -compress LZW -strip -colorspace gray ../msb_gradiente_gray.tiff
identify -verbose ../msb_gradiente_gray.tiff | grep Endianess

convert checkerg4.tiff -define tiff:fill-order=lsb -endian lsb -compress LZW -strip -colorspace gray ../lsb_checker.tiff
identify -verbose ../lsb_checker.tiff | grep Endianess

convert checkerg4.tiff -define tiff:fill-order=msb -endian msb -compress LZW -strip -colorspace gray ../msb_checker.tiff
identify -verbose ../msb_checker.tiff | grep Endianess
