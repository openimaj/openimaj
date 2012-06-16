package com.jmatio.types;

public class MLArray
{
    
    /* Matlab Array Types (Classes) */
    public static final int mxUNKNOWN_CLASS = 0;
    public static final int mxCELL_CLASS    = 1;
    public static final int mxSTRUCT_CLASS  = 2;
    public static final int mxOBJECT_CLASS  = 3;
    public static final int mxCHAR_CLASS    = 4;
    public static final int mxSPARSE_CLASS  = 5;
    public static final int mxDOUBLE_CLASS  = 6;
    public static final int mxSINGLE_CLASS  = 7;
    public static final int mxINT8_CLASS    = 8;
    public static final int mxUINT8_CLASS   = 9;
    public static final int mxINT16_CLASS   = 10;
    public static final int mxUINT16_CLASS  = 11;
    public static final int mxINT32_CLASS   = 12;
    public static final int mxUINT32_CLASS  = 13;
    public static final int mxINT64_CLASS   = 14;
    public static final int mxUINT64_CLASS  = 15;
    public static final int mxFUNCTION_CLASS = 16;
    public static final int mxOPAQUE_CLASS  = 17;
    
    public static final int mtFLAG_COMPLEX       = 0x0800;
    public static final int mtFLAG_GLOBAL        = 0x0400;
    public static final int mtFLAG_LOGICAL       = 0x0200;
    public static final int mtFLAG_TYPE          = 0xff;
    
    protected int dims[];
    public String name;
    protected int attributes;
    protected int type;
    
    public MLArray(String name, int[] dims, int type, int attributes)
    {
        this.dims = new int[dims.length];
        System.arraycopy(dims, 0, this.dims, 0, dims.length);
        
        
        if ( name != null && !name.equals("") )
        {
            this.name = name;
        }
        else
        {
            this.name = "@"; //default name
        }
        
        
        this.type = type;
        this.attributes = attributes;
    }
    
    /**
     * Gets array name
     * 
     * @return - array name
     */
    public String getName()
    {
        return name;
    }
    public int getFlags()
    { 
        int flags = type & mtFLAG_TYPE | attributes & 0xffffff00;
        
        return flags;
    }
    public byte[] getNameToByteArray()
    {
        return name.getBytes();
    }
    
    public int[] getDimensions()
    {
        int ai[] = null;
        if(dims != null)
        {
            ai = new int[dims.length];
            System.arraycopy(dims, 0, ai, 0, dims.length);
        }
        return ai;
    }

    public int getM()
    {
        int i = 0;
        if( dims != null )
        {
            i = dims[0];
        }
        return i;
    }

    public int getN()
    {
        int i = 0;
        if(dims != null)
        {
            if(dims.length > 2)
            {
                i = 1;
                for(int j = 1; j < dims.length; j++)
                {
                    i *= dims[j];
                }
            } 
            else
            {
                i = dims[1];
            }
        }
        return i;
    }

    public int getNDimensions()
    {
        int i = 0;
        if(dims != null)
        {
            i = dims.length;
        }
        return i;
    }
    public int getSize()
    {
        return getM()*getN();
    }
    public int getType()
    {
        return type;
    }

    public boolean isEmpty()
    {
        return getN() == 0;
    }
    
    public static final String typeToString(int type)
    {
        String s;
        switch (type)
        {
            case mxUNKNOWN_CLASS:
                s = "unknown";
                break;
            case mxCELL_CLASS:
                s = "cell";
                break;
            case mxSTRUCT_CLASS:
                s = "struct";
                break;
            case mxCHAR_CLASS:
                s = "char";
                break;
            case mxSPARSE_CLASS:
                s = "sparse";
                break;
            case mxDOUBLE_CLASS:
                s = "double";
                break;
            case mxSINGLE_CLASS:
                s = "single";
                break;
            case mxINT8_CLASS:
                s = "int8";
                break;
            case mxUINT8_CLASS:
                s = "uint8";
                break;
            case mxINT16_CLASS:
                s = "int16";
                break;
            case mxUINT16_CLASS:
                s = "uint16";
                break;
            case mxINT32_CLASS:
                s = "int32";
                break;
            case mxUINT32_CLASS:
                s = "uint32";
                break;
            case mxINT64_CLASS:
                s = "int64";
                break;
            case mxUINT64_CLASS:
                s = "uint64";
                break;
            case mxFUNCTION_CLASS:
                s = "function_handle";
                break;
            case mxOPAQUE_CLASS:
                s = "opaque";
                break;
            case mxOBJECT_CLASS:
                s = "object";
                break;
            default:
                s = "unknown";
                break;
        }
        return s;
    }
    
    public boolean isCell()
    {
        return type == mxCELL_CLASS;
    }

    public boolean isChar()
    {
        return type == mxCHAR_CLASS;
    }

    public boolean isComplex()
    {
        return (attributes & mtFLAG_COMPLEX) != 0;
    }

    public boolean isSparse()
    {
        return type == mxSPARSE_CLASS;
    }

    public boolean isStruct()
    {
        return type == mxSTRUCT_CLASS;
    }

    public boolean isDouble()
    {
        return type == mxDOUBLE_CLASS;
    }

    public boolean isSingle()
    {
        return type == mxSINGLE_CLASS;
    }

    public boolean isInt8()
    {
        return type == mxINT8_CLASS;
    }

    public boolean isUint8()
    {
        return type == mxUINT8_CLASS;
    }

    public boolean isInt16()
    {
        return type == mxINT16_CLASS;
    }

    public boolean isUint16()
    {
        return type == mxUINT16_CLASS;
    }

    public boolean isInt32()
    {
        return type == mxINT32_CLASS;
    }

    public boolean isUint32()
    {
        return type == mxUINT32_CLASS;
    }

    public boolean isInt64()
    {
        return type == mxINT64_CLASS;
    }

    public boolean isUint64()
    {
        return type == mxUINT64_CLASS;
    }

    public boolean isObject()
    {
        return type == mxOBJECT_CLASS;
    }

    public boolean isOpaque()
    {
        return type == mxOPAQUE_CLASS;
    }

    public boolean isLogical()
    {
        return (attributes & mtFLAG_LOGICAL) != 0;
    }

    public boolean isFunctionObject()
    {
        return type == mxFUNCTION_CLASS;
    }

    public boolean isUnknown()
    {
        return type == mxUNKNOWN_CLASS;
    }
    protected int getIndex(int m, int n)
    {
        return m+n*getM();
    }
    
    public String toString()
    {
        StringBuffer sb = new StringBuffer();
        if (dims != null)
        {
            sb.append('[');
            if (dims.length > 3)
            {
                sb.append(dims.length);
                sb.append('D');
            }
            else
            {
                sb.append(dims[0]);
                sb.append('x');
                sb.append(dims[1]);
                if (dims.length == 3)
                {
                    sb.append('x');
                    sb.append(dims[2]);
                }
            }
            sb.append("  ");
            sb.append(typeToString(type));
            sb.append(" array");
            if (isSparse())
            {
                sb.append(" (sparse");
                if (isComplex())
                {
                    sb.append(" complex");
                }
                sb.append(")");
            }
            else if (isComplex())
            {
                sb.append(" (complex)");
            }
            sb.append(']');
        }
        else
        {
            sb.append("[invalid]");
        }
        return sb.toString();
    }
    
    public String contentToString()
    {
        return "content cannot be displayed";
    }
    
    public void dispose()
    {
        
    }
    
}
