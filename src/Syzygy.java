import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/*
Syzygy endgame tablebase probing for Gambit (WDL and DTZ tables, pure Java).

The Syzygy tablebases and their file format were made by Ronald de Man. This class is a Java port of the
probing code of Fathom (https://github.com/jdart1/Fathom), which is under the MIT license:

Copyright (c) 2013-2018 Ronald de Man
Copyright (c) 2015 basil00
Copyright (c) 2016-2025 by Jon Dart

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

How to read the answers:
  probeWdl(board): 2 = side to move wins, 1 = wins but the 50-move rule saves the loser, 0 = draw,
                   -1 = loses but the 50-move rule saves it, -2 = loses. failed = no table for this position.
  probeDtz(board): number of half moves until the next capture or pawn move ("zeroing" move) on the winning path.
                   Positive = winning, negative = losing, 0 = draw, above 100 / below -100 = only a 50-move draw.
Squares inside this class are numbered a1=0 ... h8=63, so a Gambit square (a8=0) is turned over with square^56.
*/
public class Syzygy
{
    public static final int failed=-1000;
    public static final int tablebaseWin=50000;
    public static volatile int maxPieces=0;
    public static int wdlTableCount=0;
    public static int dtzTableCount=0;
    private static final int otherSideNeeded=-2000;
    private static final int wdlType=0;
    private static final int dtzType=1;
    private static final int wdlMagic=0x5d23e871;
    private static final int dtzMagic=0xa50c66d7;
    private static final int tableSlots=4096;
    private static final String pieceLetters="PNBRQK";
    private static final int[] fileToFile={0,1,2,3,3,2,1,0};
    private static final int[] wdlToMap={1,3,0,2,0};
    private static final int[] pawnMoveFlags={8,0,0,0,4};
    private static final int[] wdlToDtz={-1,-101,0,101,1};
    private static final int[] offDiagonal=
    {
        0,-1,-1,-1,-1,-1,-1,-1,1,0,-1,-1,-1,-1,-1,-1,
        1,1,0,-1,-1,-1,-1,-1,1,1,1,0,-1,-1,-1,-1,
        1,1,1,1,0,-1,-1,-1,1,1,1,1,1,0,-1,-1,
        1,1,1,1,1,1,0,-1,1,1,1,1,1,1,1,0
    };
    private static final int[] triangle=
    {
        6,0,1,2,2,1,0,6,0,7,3,4,4,3,7,0,
        1,3,8,5,5,8,3,1,2,4,5,9,9,5,4,2,
        2,4,5,9,9,5,4,2,1,3,8,5,5,8,3,1,
        0,7,3,4,4,3,7,0,6,0,1,2,2,1,0,6
    };
    private static final int[] flipDiagonal=
    {
        0,8,16,24,32,40,48,56,1,9,17,25,33,41,49,57,
        2,10,18,26,34,42,50,58,3,11,19,27,35,43,51,59,
        4,12,20,28,36,44,52,60,5,13,21,29,37,45,53,61,
        6,14,22,30,38,46,54,62,7,15,23,31,39,47,55,63
    };
    private static final int[] lowerTriangle=
    {
        28,0,1,2,3,4,5,6,0,29,7,8,9,10,11,12,
        1,7,30,13,14,15,16,17,2,8,13,31,18,19,20,21,
        3,9,14,18,32,22,23,24,4,10,15,19,22,33,25,26,
        5,11,16,20,23,25,34,27,6,12,17,21,24,26,27,35
    };
    private static final int[] diagonalIndex=
    {
        0,0,0,0,0,0,0,8,0,1,0,0,0,0,9,0,
        0,0,2,0,0,10,0,0,0,0,0,3,11,0,0,0,
        0,0,0,12,4,0,0,0,0,0,13,0,0,5,0,0,
        0,14,0,0,0,0,6,0,15,0,0,0,0,0,0,7
    };
    private static final int[] pawnFlap=
    {
        0,0,0,0,0,0,0,0,0,6,12,18,18,12,6,0,
        1,7,13,19,19,13,7,1,2,8,14,20,20,14,8,2,
        3,9,15,21,21,15,9,3,4,10,16,22,22,16,10,4,
        5,11,17,23,23,17,11,5,0,0,0,0,0,0,0,0
    };
    private static final int[] pawnTwist=
    {
        0,0,0,0,0,0,0,0,47,35,23,11,10,22,34,46,
        45,33,21,9,8,20,32,44,43,31,19,7,6,18,30,42,
        41,29,17,5,4,16,28,40,39,27,15,3,2,14,26,38,
        37,25,13,1,0,12,24,36,0,0,0,0,0,0,0,0
    };
    private static final int[][] kingPairIndex=
    {
        {
            -1,-1,-1,0,1,2,3,4,-1,-1,-1,5,6,7,8,9,
            10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,
            26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,
            42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57
        },
        {
            58,-1,-1,-1,59,60,61,62,63,-1,-1,-1,64,65,66,67,
            68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,
            84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,
            100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115
        },
        {
            116,117,-1,-1,-1,118,119,120,121,122,-1,-1,-1,123,124,125,
            126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,
            142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,
            158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173
        },
        {
            174,-1,-1,-1,175,176,177,178,179,-1,-1,-1,180,181,182,183,
            184,-1,-1,-1,185,186,187,188,189,190,191,192,193,194,195,196,
            197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,
            213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228
        },
        {
            229,230,-1,-1,-1,231,232,233,234,235,-1,-1,-1,236,237,238,
            239,240,-1,-1,-1,241,242,243,244,245,246,247,248,249,250,251,
            252,253,254,255,256,257,258,259,260,261,262,263,264,265,266,267,
            268,269,270,271,272,273,274,275,276,277,278,279,280,281,282,283
        },
        {
            284,285,286,287,288,289,290,291,292,293,-1,-1,-1,294,295,296,
            297,298,-1,-1,-1,299,300,301,302,303,-1,-1,-1,304,305,306,
            307,308,309,310,311,312,313,314,315,316,317,318,319,320,321,322,
            323,324,325,326,327,328,329,330,331,332,333,334,335,336,337,338
        },
        {
            -1,-1,339,340,341,342,343,344,-1,-1,345,346,347,348,349,350,
            -1,-1,441,351,352,353,354,355,-1,-1,-1,442,356,357,358,359,
            -1,-1,-1,-1,443,360,361,362,-1,-1,-1,-1,-1,444,363,364,
            -1,-1,-1,-1,-1,-1,445,365,-1,-1,-1,-1,-1,-1,-1,446
        },
        {
            -1,-1,-1,366,367,368,369,370,-1,-1,-1,371,372,373,374,375,
            -1,-1,-1,376,377,378,379,380,-1,-1,-1,447,381,382,383,384,
            -1,-1,-1,-1,448,385,386,387,-1,-1,-1,-1,-1,449,388,389,
            -1,-1,-1,-1,-1,-1,450,390,-1,-1,-1,-1,-1,-1,-1,451
        },
        {
            452,391,392,393,394,395,396,397,-1,-1,-1,-1,398,399,400,401,
            -1,-1,-1,-1,402,403,404,405,-1,-1,-1,-1,406,407,408,409,
            -1,-1,-1,-1,453,410,411,412,-1,-1,-1,-1,-1,454,413,414,
            -1,-1,-1,-1,-1,-1,455,415,-1,-1,-1,-1,-1,-1,-1,456
        },
        {
            457,416,417,418,419,420,421,422,-1,458,423,424,425,426,427,428,
            -1,-1,-1,-1,-1,429,430,431,-1,-1,-1,-1,-1,432,433,434,
            -1,-1,-1,-1,-1,435,436,437,-1,-1,-1,-1,-1,459,438,439,
            -1,-1,-1,-1,-1,-1,460,440,-1,-1,-1,-1,-1,-1,-1,461
        }
    };
    private static final long[][] binomial=new long[7][64];
    private static final long[][] pawnIndex=new long[6][24];
    private static final long[][] pawnFactorFile=new long[6][4];
    static
    {
        for(int i=0;i<7;i++)
        {
            for(int j=0;j<64;j++)
            {
                long top=1;
                long bottom=1;
                for(int k=0;k<i;k++)
                {
                    top*=(j-k);
                    bottom*=(k+1);
                }
                binomial[i][j]=top/bottom;
            }
        }
        for(int i=0;i<6;i++)
        {
            long sum=0;
            for(int j=0;j<24;j++)
            {
                pawnIndex[i][j]=sum;
                sum+=binomial[i][pawnTwist[(1+(j%6))*8+(j/6)]];
                if((j+1)%6==0)
                {
                    pawnFactorFile[i][j/6]=sum;
                    sum=0;
                }
            }
        }
    }
    private static final class PairsData
    {
        ByteBuffer file;
        int indexTable;
        int sizeTable;
        int data;
        int offset;
        int symbolPattern;
        int blockSize;
        int indexBits;
        int minLength;
        int constValue;
        long[] base;
        int[] symbolLength;
        int flags;
        int nextPosition;
        long indexTableBytes;
        long sizeTableBytes;
        long dataBytes;
    }
    private static final class EncodingInfo
    {
        PairsData pairs;
        final long[] factor=new long[8];
        final int[] pieces=new int[8];
        final int[] norm=new int[8];
    }
    private static final class TableEntry
    {
        long key;
        String folder;
        String name;
        int pieceCount;
        boolean symmetric;
        boolean hasPawns;
        boolean hasDtz;
        boolean kingPairEncoding;
        final int[] pawns=new int[2];
        volatile boolean wdlReady;
        volatile boolean dtzReady;
        volatile boolean wdlError;
        volatile boolean dtzError;
        EncodingInfo[] wdlEncoding;
        EncodingInfo[] dtzEncoding;
        ByteBuffer dtzFile;
        final int[] dtzFlags=new int[4];
        final int[][] dtzMapPosition=new int[4][4];
    }
    private static long[] tableKeys=new long[tableSlots];
    private static TableEntry[] tableEntries=new TableEntry[tableSlots];
    private static int slotOf(long key)
    {
        return (int)((key*0x9E3779B97F4A7C15L)>>>52);
    }
    private static TableEntry findTable(long key)
    {
        long[] keys=tableKeys;
        TableEntry[] entries=tableEntries;
        int slot=slotOf(key);
        while(entries[slot]!=null)
        {
            if(keys[slot]==key)
            {
                return entries[slot];
            }
            slot=(slot+1)&(tableSlots-1);
        }
        return null;
    }
    private static void addToHash(TableEntry entry,long key)
    {
        int slot=slotOf(key);
        while(tableEntries[slot]!=null)
        {
            slot=(slot+1)&(tableSlots-1);
        }
        tableKeys[slot]=key;
        tableEntries[slot]=entry;
    }
    private static long keyFromCounts(int[] pieceCounts,boolean mirrored)
    {
        long key=0;
        for(int pieceType=1;pieceType<=6;pieceType++)
        {
            long white=pieceCounts[pieceType];
            long black=pieceCounts[pieceType|8];
            if(mirrored)
            {
                long swap=white;
                white=black;
                black=swap;
            }
            key+=white<<(4*(pieceType-1));
            key+=black<<(4*(pieceType+5));
        }
        return key;
    }
    public static synchronized void init(String path)
    {
        maxPieces=0;
        wdlTableCount=0;
        dtzTableCount=0;
        tableKeys=new long[tableSlots];
        tableEntries=new TableEntry[tableSlots];
        if(path==null||path.isEmpty()||path.equals("<empty>"))
        {
            return;
        }
        int largest=0;
        for(String folder:path.split(File.pathSeparator))
        {
            File[] files=new File(folder.trim()).listFiles();
            if(files==null)
            {
                continue;
            }
            for(File file:files)
            {
                String fileName=file.getName();
                if(!fileName.endsWith(".rtbw")||!goodFile(file)||wdlTableCount>=tableSlots/4)
                {
                    continue;
                }
                int pieces=addTable(folder.trim(),fileName.substring(0,fileName.length()-5));
                if(pieces>largest)
                {
                    largest=pieces;
                }
            }
        }
        maxPieces=largest;
    }
    private static boolean goodFile(File file)
    {
        return file.isFile()&&(file.length()&63)==16&&file.length()<Integer.MAX_VALUE;
    }
    private static int addTable(String folder,String name)
    {
        int[] pieceCounts=new int[16];
        int color=0;
        int total=0;
        for(int i=0;i<name.length();i++)
        {
            char letter=name.charAt(i);
            if(letter=='v')
            {
                color=8;
                continue;
            }
            int pieceType=pieceLetters.indexOf(letter)+1;
            if(pieceType==0)
            {
                return 0;
            }
            pieceCounts[pieceType|color]++;
            total++;
        }
        if(total>7||pieceCounts[Board.King]!=1||pieceCounts[Board.King|8]!=1)
        {
            return 0;
        }
        long key=keyFromCounts(pieceCounts,false);
        long mirroredKey=keyFromCounts(pieceCounts,true);
        if(findTable(key)!=null)
        {
            return 0;
        }
        TableEntry entry=new TableEntry();
        entry.key=key;
        entry.folder=folder;
        entry.name=name;
        entry.pieceCount=total;
        entry.symmetric=key==mirroredKey;
        entry.hasPawns=pieceCounts[Board.Pawn]>0||pieceCounts[Board.Pawn|8]>0;
        entry.hasDtz=goodFile(new File(folder,name+".rtbz"));
        if(!entry.hasPawns)
        {
            int singles=0;
            for(int i=0;i<16;i++)
            {
                if(pieceCounts[i]==1)
                {
                    singles++;
                }
            }
            entry.kingPairEncoding=singles==2;
        }
        else
        {
            entry.pawns[0]=pieceCounts[Board.Pawn];
            entry.pawns[1]=pieceCounts[Board.Pawn|8];
            if(pieceCounts[Board.Pawn|8]>0&&(pieceCounts[Board.Pawn]==0||pieceCounts[Board.Pawn]>pieceCounts[Board.Pawn|8]))
            {
                entry.pawns[0]=pieceCounts[Board.Pawn|8];
                entry.pawns[1]=pieceCounts[Board.Pawn];
            }
        }
        addToHash(entry,key);
        if(key!=mirroredKey)
        {
            addToHash(entry,mirroredKey);
        }
        wdlTableCount++;
        if(entry.hasDtz)
        {
            dtzTableCount++;
        }
        return total;
    }
    private static int unsignedByte(ByteBuffer file,int position)
    {
        return file.get(position)&0xFF;
    }
    private static int unsignedShort(ByteBuffer file,int position)
    {
        return file.getShort(position)&0xFFFF;
    }
    private static long subfactor(long k,long n)
    {
        long top=n;
        long bottom=1;
        for(long i=1;i<k;i++)
        {
            top*=n-i;
            bottom*=i+1;
        }
        return top/bottom;
    }
    private static long initEncodingInfo(EncodingInfo info,TableEntry entry,ByteBuffer file,int position,int shift,int pawnFile)
    {
        boolean morePawns=entry.hasPawns&&entry.pawns[1]>0;
        int skip=morePawns?1:0;
        for(int i=0;i<entry.pieceCount;i++)
        {
            info.pieces[i]=(unsignedByte(file,position+i+1+skip)>>shift)&0x0f;
            info.norm[i]=0;
        }
        int order=(unsignedByte(file,position)>>shift)&0x0f;
        int secondOrder=0x0f;
        if(morePawns)
        {
            secondOrder=(unsignedByte(file,position+1)>>shift)&0x0f;
        }
        int k;
        if(entry.hasPawns)
        {
            k=entry.pawns[0];
        }
        else if(entry.kingPairEncoding)
        {
            k=2;
        }
        else
        {
            k=3;
        }
        info.norm[0]=k;
        if(morePawns)
        {
            info.norm[k]=entry.pawns[1];
            k+=info.norm[k];
        }
        for(int i=k;i<entry.pieceCount;i+=info.norm[i])
        {
            for(int j=i;j<entry.pieceCount&&info.pieces[j]==info.pieces[i];j++)
            {
                info.norm[i]++;
            }
        }
        int squaresLeft=64-k;
        long factor=1;
        for(int i=0;k<entry.pieceCount||i==order||i==secondOrder;i++)
        {
            if(i==order)
            {
                info.factor[0]=factor;
                if(entry.hasPawns)
                {
                    factor*=pawnFactorFile[info.norm[0]-1][pawnFile];
                }
                else if(entry.kingPairEncoding)
                {
                    factor*=462;
                }
                else
                {
                    factor*=31332;
                }
            }
            else if(i==secondOrder)
            {
                info.factor[info.norm[0]]=factor;
                factor*=subfactor(info.norm[info.norm[0]],48-info.norm[0]);
            }
            else
            {
                info.factor[k]=factor;
                factor*=subfactor(info.norm[k],squaresLeft);
                squaresLeft-=info.norm[k];
                k+=info.norm[k];
            }
        }
        return factor;
    }
    private static void calculateSymbolLength(PairsData pairs,int symbol,boolean[] done)
    {
        ByteBuffer file=pairs.file;
        int position=pairs.symbolPattern+3*symbol;
        int second=(unsignedByte(file,position+2)<<4)|(unsignedByte(file,position+1)>>4);
        if(second==0x0fff)
        {
            pairs.symbolLength[symbol]=0;
        }
        else
        {
            int first=((unsignedByte(file,position+1)&0xf)<<8)|unsignedByte(file,position);
            if(!done[first])
            {
                calculateSymbolLength(pairs,first,done);
            }
            if(!done[second])
            {
                calculateSymbolLength(pairs,second,done);
            }
            pairs.symbolLength[symbol]=pairs.symbolLength[first]+pairs.symbolLength[second]+1;
        }
        done[symbol]=true;
    }
    private static PairsData setupPairs(ByteBuffer file,int position,long tableSize,boolean wdl)
    {
        PairsData pairs=new PairsData();
        pairs.file=file;
        pairs.flags=unsignedByte(file,position);
        if((pairs.flags&0x80)!=0)
        {
            pairs.indexBits=0;
            pairs.constValue=wdl?unsignedByte(file,position+1):0;
            pairs.nextPosition=position+2;
            return pairs;
        }
        pairs.blockSize=unsignedByte(file,position+1);
        pairs.indexBits=unsignedByte(file,position+2);
        long realBlockCount=file.getInt(position+4)&0xFFFFFFFFL;
        long blockCount=realBlockCount+unsignedByte(file,position+3);
        int maxLength=unsignedByte(file,position+8);
        int minLength=unsignedByte(file,position+9);
        int lengths=maxLength-minLength+1;
        int symbolCount=unsignedShort(file,position+10+2*lengths);
        pairs.offset=position+10;
        pairs.symbolPattern=position+12+2*lengths;
        pairs.minLength=minLength;
        pairs.nextPosition=position+12+2*lengths+3*symbolCount+(symbolCount&1);
        long indexCount=(tableSize+(1L<<pairs.indexBits)-1)>>>pairs.indexBits;
        pairs.indexTableBytes=6*indexCount;
        pairs.sizeTableBytes=2*blockCount;
        pairs.dataBytes=realBlockCount<<pairs.blockSize;
        pairs.symbolLength=new int[symbolCount];
        boolean[] done=new boolean[symbolCount];
        for(int symbol=0;symbol<symbolCount;symbol++)
        {
            if(!done[symbol])
            {
                calculateSymbolLength(pairs,symbol,done);
            }
        }
        pairs.base=new long[lengths];
        pairs.base[lengths-1]=0;
        for(int i=lengths-2;i>=0;i--)
        {
            pairs.base[i]=(pairs.base[i+1]+unsignedShort(file,pairs.offset+2*i)-unsignedShort(file,pairs.offset+2*i+2))/2;
        }
        for(int i=0;i<lengths;i++)
        {
            pairs.base[i]<<=64-(minLength+i);
        }
        return pairs;
    }
    private static boolean initTable(TableEntry entry,int type)
    {
        ByteBuffer file;
        File path=new File(entry.folder,entry.name+(type==wdlType?".rtbw":".rtbz"));
        try(RandomAccessFile randomAccess=new RandomAccessFile(path,"r");FileChannel channel=randomAccess.getChannel())
        {
            file=channel.map(FileChannel.MapMode.READ_ONLY,0,channel.size());
            file.order(ByteOrder.LITTLE_ENDIAN);
            if(file.getInt(0)!=(type==wdlType?wdlMagic:dtzMagic))
            {
                return false;
            }
            boolean split=type==wdlType&&(unsignedByte(file,4)&0x01)!=0;
            int position=5;
            int tables=entry.hasPawns?4:1;
            EncodingInfo[] encoding=new EncodingInfo[type==wdlType?tables*2:tables];
            long[][] tableSize=new long[tables][2];
            for(int t=0;t<tables;t++)
            {
                encoding[t]=new EncodingInfo();
                tableSize[t][0]=initEncodingInfo(encoding[t],entry,file,position,0,t);
                if(split)
                {
                    encoding[tables+t]=new EncodingInfo();
                    tableSize[t][1]=initEncodingInfo(encoding[tables+t],entry,file,position,4,t);
                }
                position+=entry.pieceCount+1+((entry.hasPawns&&entry.pawns[1]>0)?1:0);
            }
            position+=position&1;
            for(int t=0;t<tables;t++)
            {
                encoding[t].pairs=setupPairs(file,position,tableSize[t][0],type==wdlType);
                position=encoding[t].pairs.nextPosition;
                if(type==dtzType)
                {
                    entry.dtzFlags[t]=encoding[t].pairs.flags;
                }
                if(split)
                {
                    encoding[tables+t].pairs=setupPairs(file,position,tableSize[t][1],true);
                    position=encoding[tables+t].pairs.nextPosition;
                }
            }
            if(type==dtzType)
            {
                for(int t=0;t<tables;t++)
                {
                    if((entry.dtzFlags[t]&2)!=0)
                    {
                        if((entry.dtzFlags[t]&16)==0)
                        {
                            for(int i=0;i<4;i++)
                            {
                                entry.dtzMapPosition[t][i]=position+1;
                                position+=1+unsignedByte(file,position);
                            }
                        }
                        else
                        {
                            position+=position&1;
                            for(int i=0;i<4;i++)
                            {
                                entry.dtzMapPosition[t][i]=position+2;
                                position+=2+2*unsignedShort(file,position);
                            }
                        }
                    }
                }
                position+=position&1;
            }
            long place=position;
            for(int t=0;t<tables;t++)
            {
                encoding[t].pairs.indexTable=(int)place;
                place+=encoding[t].pairs.indexTableBytes;
                if(split)
                {
                    encoding[tables+t].pairs.indexTable=(int)place;
                    place+=encoding[tables+t].pairs.indexTableBytes;
                }
            }
            for(int t=0;t<tables;t++)
            {
                encoding[t].pairs.sizeTable=(int)place;
                place+=encoding[t].pairs.sizeTableBytes;
                if(split)
                {
                    encoding[tables+t].pairs.sizeTable=(int)place;
                    place+=encoding[tables+t].pairs.sizeTableBytes;
                }
            }
            for(int t=0;t<tables;t++)
            {
                place=(place+0x3f)&~0x3fL;
                encoding[t].pairs.data=(int)place;
                place+=encoding[t].pairs.dataBytes;
                if(split)
                {
                    place=(place+0x3f)&~0x3fL;
                    encoding[tables+t].pairs.data=(int)place;
                    place+=encoding[tables+t].pairs.dataBytes;
                }
            }
            if(place>file.capacity())
            {
                return false;
            }
            if(type==wdlType)
            {
                entry.wdlEncoding=encoding;
            }
            else
            {
                entry.dtzEncoding=encoding;
                entry.dtzFile=file;
            }
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }
    private static int decompressPairs(PairsData pairs,long index)
    {
        if(pairs.indexBits==0)
        {
            return pairs.constValue;
        }
        ByteBuffer file=pairs.file;
        int mainIndex=(int)(index>>>pairs.indexBits);
        int literalIndex=(int)(index&((1L<<pairs.indexBits)-1))-(1<<(pairs.indexBits-1));
        int block=file.getInt(pairs.indexTable+6*mainIndex);
        literalIndex+=unsignedShort(file,pairs.indexTable+6*mainIndex+4);
        if(literalIndex<0)
        {
            while(literalIndex<0)
            {
                block--;
                literalIndex+=unsignedShort(file,pairs.sizeTable+2*block)+1;
            }
        }
        else
        {
            while(literalIndex>unsignedShort(file,pairs.sizeTable+2*block))
            {
                literalIndex-=unsignedShort(file,pairs.sizeTable+2*block)+1;
                block++;
            }
        }
        int position=pairs.data+(block<<pairs.blockSize);
        int minLength=pairs.minLength;
        long[] base=pairs.base;
        int[] symbolLength=pairs.symbolLength;
        long code=Long.reverseBytes(file.getLong(position));
        position+=8;
        int emptyBits=0;
        int symbol;
        while(true)
        {
            int length=minLength;
            while(Long.compareUnsigned(code,base[length-minLength])<0)
            {
                length++;
            }
            symbol=unsignedShort(file,pairs.offset+2*(length-minLength));
            symbol+=(int)((code-base[length-minLength])>>>(64-length));
            if(literalIndex<symbolLength[symbol]+1)
            {
                break;
            }
            literalIndex-=symbolLength[symbol]+1;
            code<<=length;
            emptyBits+=length;
            if(emptyBits>=32)
            {
                emptyBits-=32;
                long next=Integer.reverseBytes(file.getInt(position))&0xFFFFFFFFL;
                position+=4;
                code|=next<<emptyBits;
            }
        }
        int symbolPattern=pairs.symbolPattern;
        while(symbolLength[symbol]!=0)
        {
            int place=symbolPattern+3*symbol;
            int first=((unsignedByte(file,place+1)&0xf)<<8)|unsignedByte(file,place);
            if(literalIndex<symbolLength[first]+1)
            {
                symbol=first;
            }
            else
            {
                literalIndex-=symbolLength[first]+1;
                symbol=(unsignedByte(file,place+2)<<4)|(unsignedByte(file,place+1)>>4);
            }
        }
        return unsignedByte(file,symbolPattern+3*symbol)|(unsignedByte(file,symbolPattern+3*symbol+1)<<8);
    }
    private static long encode(int[] p,EncodingInfo info,TableEntry entry)
    {
        int n=entry.pieceCount;
        long index;
        int k;
        if((p[0]&0x04)!=0)
        {
            for(int i=0;i<n;i++)
            {
                p[i]^=0x07;
            }
        }
        if(!entry.hasPawns)
        {
            if((p[0]&0x20)!=0)
            {
                for(int i=0;i<n;i++)
                {
                    p[i]^=0x38;
                }
            }
            for(int i=0;i<n;i++)
            {
                if(offDiagonal[p[i]]!=0)
                {
                    if(offDiagonal[p[i]]>0&&i<(entry.kingPairEncoding?2:3))
                    {
                        for(int j=0;j<n;j++)
                        {
                            p[j]=flipDiagonal[p[j]];
                        }
                    }
                    break;
                }
            }
            if(entry.kingPairEncoding)
            {
                index=kingPairIndex[triangle[p[0]]][p[1]];
                k=2;
            }
            else
            {
                int s1=p[1]>p[0]?1:0;
                int s2=(p[2]>p[0]?1:0)+(p[2]>p[1]?1:0);
                if(offDiagonal[p[0]]!=0)
                {
                    index=triangle[p[0]]*63*62+(p[1]-s1)*62+(p[2]-s2);
                }
                else if(offDiagonal[p[1]]!=0)
                {
                    index=6*63*62+diagonalIndex[p[0]]*28*62+lowerTriangle[p[1]]*62+p[2]-s2;
                }
                else if(offDiagonal[p[2]]!=0)
                {
                    index=6*63*62+4*28*62+diagonalIndex[p[0]]*7*28+(diagonalIndex[p[1]]-s1)*28+lowerTriangle[p[2]];
                }
                else
                {
                    index=6*63*62+4*28*62+4*7*28+diagonalIndex[p[0]]*7*6+(diagonalIndex[p[1]]-s1)*6+(diagonalIndex[p[2]]-s2);
                }
                k=3;
            }
            index*=info.factor[0];
        }
        else
        {
            for(int i=1;i<entry.pawns[0];i++)
            {
                for(int j=i+1;j<entry.pawns[0];j++)
                {
                    if(pawnTwist[p[i]]<pawnTwist[p[j]])
                    {
                        int swap=p[i];
                        p[i]=p[j];
                        p[j]=swap;
                    }
                }
            }
            k=entry.pawns[0];
            index=pawnIndex[k-1][pawnFlap[p[0]]];
            for(int i=1;i<k;i++)
            {
                index+=binomial[k-i][pawnTwist[p[i]]];
            }
            index*=info.factor[0];
            if(entry.pawns[1]>0)
            {
                int t=k+entry.pawns[1];
                for(int i=k;i<t;i++)
                {
                    for(int j=i+1;j<t;j++)
                    {
                        if(p[i]>p[j])
                        {
                            int swap=p[i];
                            p[i]=p[j];
                            p[j]=swap;
                        }
                    }
                }
                long s=0;
                for(int i=k;i<t;i++)
                {
                    int square=p[i];
                    int skips=0;
                    for(int j=0;j<k;j++)
                    {
                        if(square>p[j])
                        {
                            skips++;
                        }
                    }
                    s+=binomial[i-k+1][square-skips-8];
                }
                index+=s*info.factor[k];
                k=t;
            }
        }
        while(k<n)
        {
            int t=k+info.norm[k];
            for(int i=k;i<t;i++)
            {
                for(int j=i+1;j<t;j++)
                {
                    if(p[i]>p[j])
                    {
                        int swap=p[i];
                        p[i]=p[j];
                        p[j]=swap;
                    }
                }
            }
            long s=0;
            for(int i=k;i<t;i++)
            {
                int square=p[i];
                int skips=0;
                for(int j=0;j<k;j++)
                {
                    if(square>p[j])
                    {
                        skips++;
                    }
                }
                s+=binomial[i-k+1][square-skips];
            }
            index+=s*info.factor[k];
            k=t;
        }
        return index;
    }
    private static int fillSquares(int[] foundSquares,int[] foundPieces,int found,int[] tablePieces,boolean flip,int mirror,int[] p,int i)
    {
        int pieceType=tablePieces[i]&7;
        boolean black=(tablePieces[i]>>3)!=0;
        if(flip)
        {
            black=!black;
        }
        int wanted=black?-pieceType:pieceType;
        for(int k=0;k<found;k++)
        {
            if(foundPieces[k]==wanted)
            {
                p[i++]=foundSquares[k]^mirror;
            }
        }
        return i;
    }
    private static int probeTable(Board board,int wdl,int type)
    {
        int[] foundSquares=new int[8];
        int[] foundPieces=new int[8];
        int found=0;
        long key=0;
        for(int square=0;square<64;square++)
        {
            int piece=board.squares[square^56];
            if(piece==Board.EMPTY)
            {
                continue;
            }
            if(found==7)
            {
                return failed;
            }
            foundSquares[found]=square;
            foundPieces[found]=piece;
            found++;
            if(piece>0)
            {
                key+=1L<<(4*(piece-1));
            }
            else
            {
                key+=1L<<(4*(-piece+5));
            }
        }
        if(found==2)
        {
            return type==wdlType?0:failed;
        }
        TableEntry entry=findTable(key);
        if(entry==null||(type==dtzType&&!entry.hasDtz))
        {
            return failed;
        }
        if(type==wdlType?!entry.wdlReady:!entry.dtzReady)
        {
            synchronized(entry)
            {
                if(type==wdlType?entry.wdlError:entry.dtzError)
                {
                    return failed;
                }
                if(type==wdlType?!entry.wdlReady:!entry.dtzReady)
                {
                    if(!initTable(entry,type))
                    {
                        if(type==wdlType)
                        {
                            entry.wdlError=true;
                        }
                        else
                        {
                            entry.dtzError=true;
                        }
                        return failed;
                    }
                    if(type==wdlType)
                    {
                        entry.wdlReady=true;
                    }
                    else
                    {
                        entry.dtzReady=true;
                    }
                }
            }
        }
        boolean flip;
        boolean blackSide;
        if(!entry.symmetric)
        {
            flip=key!=entry.key;
            blackSide=board.whiteToMove==flip;
        }
        else
        {
            flip=!board.whiteToMove;
            blackSide=false;
        }
        EncodingInfo[] encoding=type==wdlType?entry.wdlEncoding:entry.dtzEncoding;
        EncodingInfo info;
        int[] p=new int[8];
        int pawnFile=0;
        int flags=0;
        if(!entry.hasPawns)
        {
            if(type==dtzType)
            {
                flags=entry.dtzFlags[0];
                if(((flags&1)!=0)!=blackSide&&!entry.symmetric)
                {
                    return otherSideNeeded;
                }
                info=encoding[0];
            }
            else
            {
                info=encoding[blackSide?1:0];
            }
            for(int i=0;i<entry.pieceCount;)
            {
                int next=fillSquares(foundSquares,foundPieces,found,info.pieces,flip,0,p,i);
                if(next==i)
                {
                    return failed;
                }
                i=next;
            }
        }
        else
        {
            int mirror=flip?0x38:0;
            int i=fillSquares(foundSquares,foundPieces,found,encoding[0].pieces,flip,mirror,p,0);
            if(i!=entry.pawns[0])
            {
                return failed;
            }
            for(int j=1;j<entry.pawns[0];j++)
            {
                if(pawnFlap[p[0]]>pawnFlap[p[j]])
                {
                    int swap=p[0];
                    p[0]=p[j];
                    p[j]=swap;
                }
            }
            pawnFile=fileToFile[p[0]&7];
            if(type==dtzType)
            {
                flags=entry.dtzFlags[pawnFile];
                if(((flags&1)!=0)!=blackSide&&!entry.symmetric)
                {
                    return otherSideNeeded;
                }
                info=encoding[pawnFile];
            }
            else
            {
                info=encoding[pawnFile+(blackSide?4:0)];
            }
            while(i<entry.pieceCount)
            {
                int next=fillSquares(foundSquares,foundPieces,found,info.pieces,flip,mirror,p,i);
                if(next==i)
                {
                    return failed;
                }
                i=next;
            }
        }
        int packed;
        try
        {
            packed=decompressPairs(info.pairs,encode(p,info,entry));
        }
        catch(RuntimeException e)
        {
            return failed;
        }
        if(type==wdlType)
        {
            return (packed&0xFF)-2;
        }
        int value=(packed&0xFF)+(((packed>>8)&0x0f)<<8);
        if((flags&2)!=0)
        {
            int mapPosition=entry.dtzMapPosition[pawnFile][wdlToMap[wdl+2]];
            if((flags&16)==0)
            {
                value=unsignedByte(entry.dtzFile,mapPosition+value);
            }
            else
            {
                value=unsignedShort(entry.dtzFile,mapPosition+2*value);
            }
        }
        if((flags&pawnMoveFlags[wdl+2])==0||(wdl&1)!=0)
        {
            value*=2;
        }
        return value;
    }
    private static int probeAlphaBeta(Board board,int alpha,int beta)
    {
        for(int move:MoveGenerator.generateLegalMoves(board))
        {
            if(!Move.unbitpackcapturePosition(move))
            {
                continue;
            }
            board.makeMove(move);
            int value=probeAlphaBeta(board,-beta,-alpha);
            board.unmakeMove();
            if(value==failed)
            {
                return failed;
            }
            value=-value;
            if(value>alpha)
            {
                if(value>=beta)
                {
                    return value;
                }
                alpha=value;
            }
        }
        int value=probeTable(board,0,wdlType);
        if(value==failed)
        {
            return failed;
        }
        return Math.max(alpha,value);
    }
    private static final int bestIsCapture=16;
    private static int probeWdlWithFlag(Board board)
    {
        int[] moves=MoveGenerator.generateLegalMoves(board);
        int bestCapture=-3;
        int bestEnPassant=-3;
        for(int move:moves)
        {
            if(!Move.unbitpackcapturePosition(move))
            {
                continue;
            }
            board.makeMove(move);
            int value=probeAlphaBeta(board,-2,-bestCapture);
            board.unmakeMove();
            if(value==failed)
            {
                return failed;
            }
            value=-value;
            if(value>bestCapture)
            {
                if(value==2)
                {
                    return 2+bestIsCapture;
                }
                if(!Move.unbitpackenPassant(move))
                {
                    bestCapture=value;
                }
                else if(value>bestEnPassant)
                {
                    bestEnPassant=value;
                }
            }
        }
        int value=probeTable(board,0,wdlType);
        if(value==failed)
        {
            return failed;
        }
        if(bestEnPassant>bestCapture)
        {
            if(bestEnPassant>value)
            {
                return bestEnPassant+bestIsCapture;
            }
            bestCapture=bestEnPassant;
        }
        if(bestCapture>=value)
        {
            if(bestCapture>0)
            {
                return bestCapture+bestIsCapture;
            }
            return bestCapture;
        }
        if(bestEnPassant>-3&&value==0)
        {
            boolean otherMove=false;
            for(int move:moves)
            {
                if(!Move.unbitpackenPassant(move))
                {
                    otherMove=true;
                    break;
                }
            }
            if(!otherMove&&!MoveGenerator.isKingAttacked(board,board.whiteToMove))
            {
                return bestEnPassant+bestIsCapture;
            }
        }
        return value;
    }
    private static boolean canProbe(Board board)
    {
        return maxPieces>0&&board.pieceCount<=maxPieces&&!board.whiteKingside&&!board.whiteQueenside&&!board.blackKingside&&!board.blackQueenside;
    }
    public static int probeWdl(Board board)
    {
        if(!canProbe(board))
        {
            return failed;
        }
        int value=probeWdlWithFlag(board);
        if(value==failed)
        {
            return failed;
        }
        if(value>4)
        {
            value-=bestIsCapture;
        }
        return value;
    }
    private static boolean isMate(Board board)
    {
        return MoveGenerator.isKingAttacked(board,board.whiteToMove)&&MoveGenerator.generateLegalMoves(board).length==0;
    }
    public static int probeDtz(Board board)
    {
        if(!canProbe(board))
        {
            return failed;
        }
        return probeDtzInside(board);
    }
    private static int probeDtzInside(Board board)
    {
        int wdl=probeWdlWithFlag(board);
        if(wdl==failed)
        {
            return failed;
        }
        boolean captureIsBest=wdl>4;
        if(captureIsBest)
        {
            wdl-=bestIsCapture;
        }
        if(wdl==0)
        {
            return 0;
        }
        if(captureIsBest)
        {
            return wdlToDtz[wdl+2];
        }
        int[] moves=MoveGenerator.generateLegalMoves(board);
        if(wdl>0)
        {
            for(int move:moves)
            {
                if(Math.abs(board.squares[Move.unbitpackcurrentPosition(move)])!=Board.Pawn||Move.unbitpackcapturePosition(move))
                {
                    continue;
                }
                board.makeMove(move);
                int value=probeWdlWithFlag(board);
                board.unmakeMove();
                if(value==failed)
                {
                    return failed;
                }
                if(value>4)
                {
                    value-=bestIsCapture;
                }
                if(-value==wdl)
                {
                    return wdlToDtz[wdl+2];
                }
            }
        }
        int dtz=probeTable(board,wdl,dtzType);
        if(dtz==failed)
        {
            return failed;
        }
        if(dtz!=otherSideNeeded)
        {
            return wdlToDtz[wdl+2]+(wdl>0?dtz:-dtz);
        }
        int best;
        if(wdl>0)
        {
            best=Integer.MAX_VALUE;
        }
        else
        {
            best=wdlToDtz[wdl+2];
        }
        for(int move:moves)
        {
            if(Move.unbitpackcapturePosition(move)||Math.abs(board.squares[Move.unbitpackcurrentPosition(move)])==Board.Pawn)
            {
                continue;
            }
            board.makeMove(move);
            int value=probeDtzInside(board);
            boolean mate=value!=failed&&-value==1&&isMate(board);
            board.unmakeMove();
            if(value==failed)
            {
                return failed;
            }
            value=-value;
            if(mate)
            {
                best=1;
            }
            else if(wdl>0)
            {
                if(value>0&&value+1<best)
                {
                    best=value+1;
                }
            }
            else
            {
                if(value-1<best)
                {
                    best=value-1;
                }
            }
        }
        return best;
    }
    /*
    Gives every legal move of the position a rank (higher = better) from the tablebases:
    1000 = sure win, 900 to 999 = win that is running close to the 50-move rule, 1 to 899 = win spoiled by the 50-move rule,
    0 = draw, negative = the same for losing. Returns 2 if the DTZ tables answered, 1 if only the WDL tables could,
    0 if the tablebases cannot help here (then tablebaseRank is not usable).
    */
    public static int rankRootMoves(Board board,int[] rootMoves,int[] tablebaseRank,boolean hasRepeated)
    {
        if(!canProbe(board)||rootMoves.length==0)
        {
            return 0;
        }
        int clock=board.halfmoveClock;
        boolean dtzWorked=true;
        for(int i=0;i<rootMoves.length&&dtzWorked;i++)
        {
            board.makeMove(rootMoves[i]);
            int value;
            if(board.halfmoveClock==0)
            {
                value=probeWdl(board);
                if(value!=failed)
                {
                    value=wdlToDtz[-value+2];
                }
            }
            else
            {
                value=probeDtzInside(board);
                if(value!=failed)
                {
                    value=-value;
                    if(value>0)
                    {
                        value++;
                    }
                    else if(value<0)
                    {
                        value--;
                    }
                }
            }
            if(value==2&&isMate(board))
            {
                value=1;
            }
            board.unmakeMove();
            if(value==failed)
            {
                dtzWorked=false;
                break;
            }
            int rank=0;
            if(value>0)
            {
                if(value+clock<=99&&!hasRepeated)
                {
                    rank=1000;
                }
                else
                {
                    rank=1000-(value+clock);
                }
            }
            else if(value<0)
            {
                if(-value*2+clock<100)
                {
                    rank=-1000;
                }
                else
                {
                    rank=-1000+(-value+clock);
                }
            }
            tablebaseRank[i]=rank;
        }
        if(dtzWorked)
        {
            return 2;
        }
        int[] wdlToRank={-1000,-899,0,899,1000};
        for(int i=0;i<rootMoves.length;i++)
        {
            board.makeMove(rootMoves[i]);
            int value=probeWdl(board);
            board.unmakeMove();
            if(value==failed)
            {
                return 0;
            }
            tablebaseRank[i]=wdlToRank[-value+2];
        }
        return 1;
    }
    public static int scoreFromRank(int rank)
    {
        if(rank>=900)
        {
            return tablebaseWin;
        }
        if(rank>0)
        {
            return Math.max(3,rank-800)/2;
        }
        if(rank==0)
        {
            return 0;
        }
        if(rank>-900)
        {
            return Math.min(-3,rank+800)/2;
        }
        return -tablebaseWin;
    }
}
