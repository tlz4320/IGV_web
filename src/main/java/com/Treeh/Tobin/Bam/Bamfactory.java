package com.Treeh.Tobin.Bam;

import htsjdk.samtools.*;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static htsjdk.samtools.BAMRecordCodec.makeSentinelCigar;

public class Bamfactory {
    private char getIntegerType(long val) {
        if (val > 4294967295L) {
            throw new IllegalArgumentException("Integer attribute value too large to be encoded in BAM");
        } else if (val > 2147483647L) {
            return 'I';
        } else if (val > 65535L) {
            return 'i';
        } else if (val > 32767L) {
            return 'S';
        } else if (val > 255L) {
            return 's';
        } else if (val > 127L) {
            return 'C';
        } else if (val >= -128L) {
            return 'c';
        } else if (val >= -32768L) {
            return 's';
        } else if (val >= -2147483648L) {
            return 'i';
        } else {
            throw new IllegalArgumentException("Integer attribute value too negative to be encoded in BAM");
        }
    }
    private char getTagValueType(Object value) {
        if (value instanceof String) {
            return 'Z';
        } else if (value instanceof Character) {
            return 'A';
        } else if (value instanceof Float) {
            return 'f';
        } else if (value instanceof Number) {
            if (!(value instanceof Byte) && !(value instanceof Short) && !(value instanceof Integer) && !(value instanceof Long)) {
                throw new IllegalArgumentException("Unrecognized tag type " + value.getClass().getName());
            } else {
                return getIntegerType(((Number) value).longValue());
            }
        } else if (!(value instanceof byte[]) && !(value instanceof short[]) && !(value instanceof int[]) && !(value instanceof float[])) {
            throw new IllegalArgumentException("When writing BAM, unrecognized tag type " + value.getClass().getName());
        } else {
            return 'B';
        }
    }
    private int getBinaryValueSize(Object attributeValue) {
        switch (getTagValueType(attributeValue)) {
            case 'A':
                return 1;
            case 'B':
                int numElements = Array.getLength(attributeValue);
                byte elementSize;
                if (attributeValue instanceof byte[]) {
                    elementSize = 1;
                } else if (attributeValue instanceof short[]) {
                    elementSize = 2;
                } else if (attributeValue instanceof int[]) {
                    elementSize = 4;
                } else {
                    if (!(attributeValue instanceof float[])) {
                        throw new IllegalArgumentException("Unsupported array type: " + attributeValue.getClass());
                    }

                    elementSize = 4;
                }

                return numElements * elementSize + 5;
            case 'C':
            case 'c':
                return 1;
            case 'H':
                byte[] byteArray = (byte[]) ((byte[]) attributeValue);
                return byteArray.length * 2 + 1;
            case 'I':
            case 'i':
                return 4;
            case 'S':
            case 's':
                return 2;
            case 'Z':
                return ((String) attributeValue).length() + 1;
            case 'f':
                return 4;
            default:
                throw new IllegalArgumentException("When writing BAM, unrecognized tag type " + attributeValue.getClass().getName());
        }
    }
    private int[] encode(Cigar cigar) {
        if (cigar.numCigarElements() == 0) {
            return new int[0];
        } else {
            int[] binaryCigar = new int[cigar.numCigarElements()];
            int binaryCigarLength = 0;

            for(int i = 0; i < cigar.numCigarElements(); ++i) {
                CigarElement cigarElement = cigar.getCigarElement(i);
                int op = CigarOperator.enumToBinary(cigarElement.getOperator());
                binaryCigar[binaryCigarLength++] = cigarElement.getLength() << 4 | op;
            }

            return binaryCigar;
        }
    }
    private int computeIndexingBin(SAMRecord alignment) {
        int alignmentStart = alignment.getAlignmentStart() - 1;
        int alignmentEnd = alignment.getAlignmentEnd();
        if (alignmentEnd <= 0) {
            alignmentEnd = alignmentStart + 1;
        }

        if (alignmentStart <= 536870912 && alignmentEnd <= 536870912) {
            return GenomicIndexUtil.regionToBin(alignmentStart, alignmentEnd) & '\uffff';
        } else {
            throw new IllegalStateException("Read position too high for BAI bin indexing.");
        }
    }
    int writeInt(byte[] res, int offset, int data){
        res[offset] = (byte)data;
        res[offset + 1] = (byte)(data >> 8);
        res[offset + 2] = (byte)(data >> 16);
        res[offset + 3] = (byte)(data >> 24);
        return offset + 4;
    }
    int writeUByte(byte[] res, int offset, byte data){
        res[offset] = data;
        return offset + 1;
    }
    int writeFloat(byte[] res, int offset, float data){
        byte []b = ByteBuffer.allocate(4).putFloat(data).array();
        res[offset] = b[0];
        res[offset + 1] = b[1];
        res[offset + 2] = b[2];
        res[offset + 3] = b[3];
        return offset + 4;
    }
    int writeUShort(byte[] res, int offset, short data){
        res[offset] = (byte)data;
        res[offset + 1] = (byte)(data >> 8);
        return offset + 2;
    }
    int writeString(byte[] res, int offset, String s){
        for(byte b : s.getBytes())
            res[offset++] = b;
        res[offset++] = 0;
        return offset;
    }
    int writeArray(byte[] res, int offset, Object data){

        if (data instanceof byte[]) {
            offset = writeUByte(res, offset, (byte)99);
            offset = writeInt(res, offset, ((byte[]) data).length);
            for(byte b : (byte[])data)
                offset = writeUByte(res, offset,b);
        } else if (data instanceof short[]) {
            offset = writeUByte(res, offset, (byte)115);
            offset = writeInt(res, offset, ((short[]) data).length);
            for(short s :(short[]) data)
                offset = writeUShort(res, offset, s);
        } else if (data instanceof int[]) {
            offset = writeUByte(res, offset, (byte)105);
            offset = writeInt(res, offset, ((int[]) data).length);
            for(int s :(int[]) data)
                offset = writeInt(res, offset, s);
        } else {
            if (!(data instanceof float[])) {
                throw new SAMException("Unrecognized array value type: " + data.getClass());
            }

            offset = writeUByte(res, offset, (byte)102);
            offset = writeInt(res, offset, ((float[]) data).length);
            for(float s :(float[]) data)
                offset = writeFloat(res, offset, s);
        }
        return offset;
    }
     int[] cigar_encode(Cigar cigar) {
        if (cigar.numCigarElements() == 0) {
            return new int[0];
        } else {
            int[] binaryCigar = new int[cigar.numCigarElements()];
            int binaryCigarLength = 0;

            for(int i = 0; i < cigar.numCigarElements(); ++i) {
                CigarElement cigarElement = cigar.getCigarElement(i);
                int op = CigarOperator.enumToBinary(cigarElement.getOperator());
                binaryCigar[binaryCigarLength++] = cigarElement.getLength() << 4 | op;
            }

            return binaryCigar;
        }
    }
    private byte charToCompressedBaseHigh(byte base) {
        switch(base) {
            case 46:
            case 78:
            case 110:
                return -16;
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 62:
            case 63:
            case 64:
            case 69:
            case 70:
            case 73:
            case 74:
            case 76:
            case 79:
            case 80:
            case 81:
            case 85:
            case 88:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 101:
            case 102:
            case 105:
            case 106:
            case 108:
            case 111:
            case 112:
            case 113:
            case 117:
            case 120:
            default:
                throw new IllegalArgumentException("Bad base passed to charToCompressedBaseHigh: " + Character.toString((char)base) + "(" + base + ")");
            case 61:
                return 0;
            case 65:
            case 97:
                return 16;
            case 66:
            case 98:
                return -32;
            case 67:
            case 99:
                return 32;
            case 68:
            case 100:
                return -48;
            case 71:
            case 103:
                return 64;
            case 72:
            case 104:
                return -80;
            case 75:
            case 107:
                return -64;
            case 77:
            case 109:
                return 48;
            case 82:
            case 114:
                return 80;
            case 83:
            case 115:
                return 96;
            case 84:
            case 116:
                return -128;
            case 86:
            case 118:
                return 112;
            case 87:
            case 119:
                return -112;
            case 89:
            case 121:
                return -96;
        }
    }
    private byte charToCompressedBaseLow(byte base) {
        switch(base) {
            case 46:
            case 78:
            case 110:
                return 15;
            case 47:
            case 48:
            case 49:
            case 50:
            case 51:
            case 52:
            case 53:
            case 54:
            case 55:
            case 56:
            case 57:
            case 58:
            case 59:
            case 60:
            case 62:
            case 63:
            case 64:
            case 69:
            case 70:
            case 73:
            case 74:
            case 76:
            case 79:
            case 80:
            case 81:
            case 85:
            case 88:
            case 90:
            case 91:
            case 92:
            case 93:
            case 94:
            case 95:
            case 96:
            case 101:
            case 102:
            case 105:
            case 106:
            case 108:
            case 111:
            case 112:
            case 113:
            case 117:
            case 120:
            default:
                throw new IllegalArgumentException("Bad base passed to charToCompressedBaseLow: " + Character.toString((char)base) + "(" + base + ")");
            case 61:
                return 0;
            case 65:
            case 97:
                return 1;
            case 66:
            case 98:
                return 14;
            case 67:
            case 99:
                return 2;
            case 68:
            case 100:
                return 13;
            case 71:
            case 103:
                return 4;
            case 72:
            case 104:
                return 11;
            case 75:
            case 107:
                return 12;
            case 77:
            case 109:
                return 3;
            case 82:
            case 114:
                return 5;
            case 83:
            case 115:
                return 6;
            case 84:
            case 116:
                return 8;
            case 86:
            case 118:
                return 7;
            case 87:
            case 119:
                return 9;
            case 89:
            case 121:
                return 10;
        }
    }
    byte[] bytesToCompressedBases(byte[] readBases) {
        byte[] compressedBases = new byte[(readBases.length + 1) / 2];

        int i;
        for(i = 1; i < readBases.length; i += 2) {
            compressedBases[i / 2] = (byte)(charToCompressedBaseHigh(readBases[i - 1]) | charToCompressedBaseLow(readBases[i]));
        }

        if (i == readBases.length) {
            compressedBases[i / 2] = charToCompressedBaseHigh(readBases[i - 1]);
        }

        return compressedBases;
    }

    public int writeTag(String tag, Object value, byte[] res, int index) {
        byte[] tagbytes = tag.getBytes();
        if (tagbytes.length == 0)
            res[index++] = 0;
        else
            res[index++] = tagbytes[0];
        if (tagbytes.length < 2)
            res[index++] = 0;
        else
            res[index++] = tagbytes[1];

        char tagValueType = getTagValueType(value);
        index = writeUByte(res, index, (byte) tagValueType);
        switch (tagValueType) {
            case 'A':
                index = writeUByte(res, index, (byte)((Character)value).charValue());
                break;
            case 'B':
                index = writeArray(res, index, value);
                break;
            case 'C':
                index = writeUByte(res,index, (byte)((Integer)value).shortValue());
                break;
            case 'I':
                index = writeInt(res, index, ((Long)value).intValue());
                break;
            case 'S':
                index = writeUShort(res,index, ((Number) value).shortValue());
                break;
            case 'Z':
                index = writeString(res, index, (String) value);
                break;
            case 'c':
                index = writeUByte(res,index, ((Number) value).byteValue());
                break;
            case 'f':
               index = writeFloat(res, index, ((Float)value));
                break;
            case 'i':
                index = writeInt(res,index, (short)((Number) value).intValue());
                break;
            case 's':
                index = writeUShort(res,index, ((Number) value).shortValue());
                break;
            default:
                throw new IllegalArgumentException("When writing BAM, unrecognized tag type " + value.getClass().getName());
        }
        return index;
    }
    public byte[] sam2bam(SAMRecord alignment){
        int readLength = alignment.getReadLength();
        boolean cigarSwitcharoo = alignment.getCigar().numCigarElements() > 65535;
        Cigar cigarToByte;
        if (cigarSwitcharoo) {
            int[] cigarEncoding = encode(alignment.getCigar());
            alignment.setAttribute(SAMTag.CG.name(), cigarEncoding);
            cigarToByte = makeSentinelCigar(alignment.getCigar());
        } else {
            cigarToByte = alignment.getCigar();
        }
        int blockSize = 32 + alignment.getReadNameLength() + 1 + cigarToByte.numCigarElements() * 4 + (readLength + 1) / 2 + readLength;
        int attributesSize = alignment.getAttributesBinarySize();
        if (attributesSize != -1) {
            blockSize += attributesSize;
        } else {
            for(SAMRecord.SAMTagAndValue attribute:  alignment.getAttributes()) {
                blockSize += (3 + getBinaryValueSize(attribute.value));
            }
        }

        int indexBin = 0;
        SAMSequenceRecord sequence = alignment.getHeader() != null ? alignment.getHeader().getSequence(alignment.getReferenceName()) : null;
        boolean tooLarge = sequence != null && SAMUtils.isReferenceSequenceIncompatibleWithBAI(sequence);
        if (alignment.getAlignmentStart() != 0 && !tooLarge) {
            indexBin = computeIndexingBin(alignment);
        }
        byte[] res = new byte[blockSize + 4];
        int index = 0;
        index = writeInt(res, index, blockSize);
        index = writeInt(res, index, alignment.getReferenceIndex());
        index = writeInt(res, index, alignment.getAlignmentStart() - 1);
        index = writeUByte(res, index, (byte)(alignment.getReadNameLength() + 1));
        index = writeUByte(res, index, (byte)(alignment.getMappingQuality()));
        index = writeUShort(res,index, (short)indexBin);
        index = writeUShort(res,index, (short)cigarToByte.numCigarElements());
        index = writeUShort(res,index, (short) alignment.getFlags());
        index = writeInt(res,index, alignment.getReadLength());
        index = writeInt(res,index, alignment.getMateReferenceIndex());
        index = writeInt(res,index, alignment.getMateAlignmentStart() - 1);
        index = writeInt(res,index, alignment.getInferredInsertSize());
        index = writeString(res, index, alignment.getReadName());
        int [] binaryCigar = cigar_encode(cigarToByte);
        for(int i = 0; i < binaryCigar.length; i++)
            index = writeInt(res, index, binaryCigar[i]);
        byte[] bases = bytesToCompressedBases(alignment.getReadBases());
        for(byte b : bases)
            index = writeUByte(res, index, b);
        byte[] qualities = alignment.getBaseQualities();
        if (qualities.length == 0) {
            qualities = new byte[alignment.getReadLength()];
            Arrays.fill(qualities, (byte)-1);
        }
        for(byte b : qualities)
            index = writeUByte(res, index, b);
        for(SAMRecord.SAMTagAndValue attribute:  alignment.getAttributes()) {
            index = writeTag(attribute.tag, attribute.value, res, index);
        }
        return res;
    }
}
