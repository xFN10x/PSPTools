package psptools.psp.sfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NameNotFoundException;

import com.google.common.io.LittleEndianDataInputStream;
import com.google.gson.GsonBuilder;

public class ParamSFO {

    public static transient final short UTF8_S = 4;
    public static transient final short UTF8 = 516;
    public static transient final short INT32 = 1028;
    public static transient final Map<String, String> CATEGORYS = new HashMap<String, String>();
    static {
        CATEGORYS.put("AP", "App Photo");
        CATEGORYS.put("AM", "App Music");
        CATEGORYS.put("AV", "App Video");
        CATEGORYS.put("BV", "Broadcast Video");
        CATEGORYS.put("AT", "App TV");
        CATEGORYS.put("WT", "Web TV");
        CATEGORYS.put("HG", "HDD Game");
        CATEGORYS.put("CB", "CELL BE?");
        CATEGORYS.put("AS", "Playstation Now");
        CATEGORYS.put("HM", "Playstation Home");
        CATEGORYS.put("SF", "Playstation Store");
        CATEGORYS.put("2G", "PS2 Game");
        CATEGORYS.put("2P", "PS2 PSN Game");
        CATEGORYS.put("1P", "PS1 PSN Game");
        CATEGORYS.put("MN", "PSP Mini");
        CATEGORYS.put("PE", "PSP Emulator");
        CATEGORYS.put("PP", "PSP Game");
        CATEGORYS.put("GD", "Game Data");
        CATEGORYS.put("2D", "PS2 Data");
        CATEGORYS.put("HG", "HDD Game");
        CATEGORYS.put("SD", "Save Data");
        //psp
        CATEGORYS.put("MS", "MemoryStick Save");
        CATEGORYS.put("MG", "MemoryStick Game");

        CATEGORYS.put("WG", "WLAN Game");

        CATEGORYS.put("UG", "UMD Game");
        CATEGORYS.put("UV", "UMD Video");
        CATEGORYS.put("UA", "UMD Audio");
        CATEGORYS.put("UC", "UMD Cleaning Disc");

    }

    public Header header;
    public List<IndexEntry> index_table = new ArrayList<IndexEntry>();
    public Map<String, byte[]> data = new HashMap<String, byte[]>();
    public Map<String, Integer> dataTypes = new HashMap<String, Integer>();

    public static class Header {
        public String magic;
        public String version;
        public long key_table_start;
        public long data_table_start;
        public long table_entries;
    }

    public static class Params { //IVE FOUND THE MOTHER LOAD https://hitmen.c02.at/files/yapspd/psp_doc/chap26.html#sec26.4
        public static final transient String Category = "CATEGORY";
        public static final transient String Title = "TITLE";
        public static final transient String Parental_Level = "PARENTAL_LEVEL";

        //save specific
        public static final transient String Description = "SAVEDATA_DETAIL";
        public static final transient String SaveFolderName = "SAVEDATA_DIRECTORY";
        public static final transient String SaveFiles = "SAVEDATA_FILE_LIST";
        public static final transient String SaveParams = "SAVEDATA_PARAMS";
        public static final transient String SaveTitle = "SAVEDATA_TITLE";

        //umd game specific
        public static final transient String AppVersion = "APP_VER";
        public static final transient String InstantBoot = "BOOTABLE";

        public static final transient String DiscID = "DISC_ID";
        public static final transient String DiscNumber = "DISC_NUMBER";
        public static final transient String TotalDiscs = "DISC_TOTAL";
        public static final transient String DiscVersion = "DISC_VERSION";
        public static final transient String HRKGMP_VER = "HRKGMP_VER";

        public static final transient String MinimumFirmwareVersion = "PSP_SYSTEM_VER";
        public static final transient String Region = "REGION";
        public static final transient String UseUsb = "USE_USB";

    }

    public static class IndexEntry {
        public int keyOffset;
        public int dataFmt;
        public long dataLength;
        public long dataMaxLength;
        public long dataOffset;

    }

    public String toString() {
        return new GsonBuilder().setPrettyPrinting().create().toJson(this);
    }

    public static ParamSFO ofFile(File of) throws IOException {
        if (!of.exists())
            return null;
        return ParamSFO.ofStream(Files.newInputStream(of.toPath()));
    }

    public static ParamSFO ofStream(InputStream stream) throws IOException {
        //System.out.println("READING NEW -----------------: ");
        LittleEndianDataInputStream Stream = new LittleEndianDataInputStream(stream);
        int currentByte = 0;

        ParamSFO toBuild = new ParamSFO();
        Header paramHeader = new Header();

        paramHeader.magic = new String(Stream.readNBytes(4), StandardCharsets.UTF_8); // 4

        byte[] versionBytes = Stream.readNBytes(4); // 4
        float version = versionBytes[0] + (versionBytes[1] / 100f);
        paramHeader.version = String.valueOf(version);

        paramHeader.key_table_start = Integer.toUnsignedLong(Stream.readInt()); // 4
        paramHeader.data_table_start = Integer.toUnsignedLong(Stream.readInt()); // 4
        paramHeader.table_entries = Integer.toUnsignedLong(Stream.readInt()); // 4

        currentByte = 20;

        for (int i = 0; i < paramHeader.table_entries; i++) { // read all index_table elements
            IndexEntry toAdd = new IndexEntry();

            toAdd.keyOffset = Stream.readUnsignedShort(); // 2
            toAdd.dataFmt = Stream.readUnsignedShort(); // 2
            toAdd.dataLength = Integer.toUnsignedLong(Stream.readInt()); // 4
            toAdd.dataMaxLength = Integer.toUnsignedLong(Stream.readInt()); // 4
            toAdd.dataOffset = Integer.toUnsignedLong(Stream.readInt()); // 4
            toBuild.index_table.add(toAdd);
            currentByte += 16;
        }

        List<Map.Entry<String, byte[]>> list = new ArrayList<Map.Entry<String, byte[]>>();
        for (IndexEntry ie : toBuild.index_table) { // read all key tables
            int key_length;
            if (toBuild.index_table.indexOf(ie) + 1 < toBuild.index_table.size())
                key_length = toBuild.index_table.get(toBuild.index_table.indexOf(ie) + 1).keyOffset - ie.keyOffset;
            else
                key_length = (int) (paramHeader.data_table_start - (ie.keyOffset + paramHeader.key_table_start));
            String key = new String(Stream.readNBytes(key_length), StandardCharsets.UTF_8).trim();
            Map.Entry<String, byte[]> Entry = new AbstractMap.SimpleEntry<String, byte[]>(
                    key, null);
            list.add(Entry);
            //System.out.println(ie.dataFmt);
            toBuild.dataTypes.put(key, ie.dataFmt);
            currentByte += key_length;
        }

        Stream.skip((4 - (currentByte % 4)) % 4);

        for (IndexEntry ie : toBuild.index_table) { // read all data
            var entryi = toBuild.index_table.indexOf(ie);
            var toSkip = ie.dataMaxLength - ie.dataLength;

            byte[] bytes = Stream.readNBytes((int) ie.dataLength);
            list.get(entryi).setValue(bytes);

            list.get(entryi).setValue(bytes);
            Stream.skip(toSkip);

        }

        for (Map.Entry<String, byte[]> entry : list) {
            toBuild.data.put(entry.getKey(), entry.getValue());
            System.out.println(entry.getKey()+"="+entry.getValue());
        }

        Stream.close();
        stream.close();
        toBuild.header = paramHeader;
        return toBuild;
    }

    public Object getParam(String param) throws NameNotFoundException {
        if (!data.containsKey(param)) {
            throw new NameNotFoundException("Param: " + param + ", not found in SFO. Category: " + getParam(Params.Category) );
        }
        return paramBytesToValue(data.get(param), dataTypes.get(param), false);
    }

    public Object getParam(String param, boolean htmlText) throws NameNotFoundException {
        if (!data.containsKey(param)) {
            throw new NameNotFoundException("Param: " + param + ", not found in SFO. Category: " + getParam(Params.Category) );
        }
        return paramBytesToValue(data.get(param), dataTypes.get(param), htmlText);
    }

    public static Object paramBytesToValue(byte[] val, int fmt, boolean html) {
        if (html)
            switch (fmt) {
                case UTF8_S:
                    String notNullTerm = new String(val);
                    return "<html>" + notNullTerm.substring(0, notNullTerm.indexOf("\u0000")).replace("\n", "<br>")
                            + "</html>";
                case UTF8:
                    return "<html>" + new String(val).replace("\n", "<br>") + "</html>";

                case INT32:
                    return ByteBuffer.wrap(val).order(ByteOrder.LITTLE_ENDIAN).getInt();

                default:
                    throw new IllegalAccessError("Format: " + fmt + ", is not valid.");
            }
        else
            switch (fmt) {
                case UTF8_S:
                    String notNullTerm = new String(val);
                    return notNullTerm.substring(0, notNullTerm.indexOf("\u0000"));
                case UTF8:
                    return new String(val);

                case INT32:
                    return ByteBuffer.wrap(val).order(ByteOrder.LITTLE_ENDIAN).getInt();

                default:
                    throw new IllegalAccessError("Format: " + fmt + ", is not valid.");
            }
    }

}
