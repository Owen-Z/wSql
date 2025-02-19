package com.Data;

import com.DBMS.proto.DBMS;
import org.apache.commons.lang3.SerializationUtils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DataInsert {
    public String tbName;
    public String dbName;

    public DataInsert(String dbName,String tbName){
        this.dbName = dbName;
        this.tbName = tbName;
    }

    public boolean DateCheck(HashMap<String,String> map){
        File file = new File("src\\DBMS_ROOT\\data\\"+dbName + "\\" + tbName + ".ibd");
        if (file.exists()){
            try {
                FileInputStream input = new FileInputStream(file);
                byte[] buffer = new byte[10240];
                input.read(buffer);
                input.close();
                DBMS.Table table = SerializationUtils.deserialize(buffer);
                List<DBMS.Table.Column> columnList = new ArrayList<>(table.getColumnList());
                int size = map.size();
                for(DBMS.Table.Column column : columnList){
                    if (map.containsKey(column.getColumnName())){
                        String type = column.getType();
                        String val = map.get(column.getColumnName());
                        System.out.println(column.getPrimary().equals("true"));
                        System.out.println(column.getUnique().equals("true"));
                        if(column.getPrimary().equals("true") || column.getUnique().equals("true")){
                            List<String> list = new ArrayList<>(column.getValList());
                            HashSet set = new HashSet(list);
                            if (set.contains(val)){
                                return false;
                            }
                        }
                        if(!column.getForeign().equals("false")){
                            String[] ss = column.getForeign().split(",");
                            File file1 = new File("src\\DBMS_ROOT\\data\\"+dbName + "\\" + ss[0] + ".ibd");
                            FileInputStream input1 = new FileInputStream(file1);
                            byte[] buffer1 = new byte[10240];
                            input1.read(buffer1);
                            input1.close();
                            DBMS.Table table1 = SerializationUtils.deserialize(buffer);
                            List<DBMS.Table.Column> columnList1 = new ArrayList<>(table1.getColumnList());
                            boolean t = false;
                            for (DBMS.Table.Column column1 : columnList1){
                                if (column1.getColumnName().equals(ss[1])){
                                    List<String> strings = new ArrayList<>(column1.getValList());
                                    for (String s:strings){
                                        if (s.equals(val)){
                                            t = true;
                                        }
                                    }
                                }
                            }
                            if(!t){
                                return false;
                            }
                        }
                        if(type.equals("INTEFER")){
                            try {
                                int i = Integer.parseInt(val);
                            }catch (Exception e){
                                return false;
                            }
                        }else if(type.equals("BOOL")){
                            if(!(val.equals("xTRUE") || val.equals("xFALSE"))){
                                return false;
                            }
                        }else if(type.equals("DOUBLE")){
                            try {
                                float i = Float.parseFloat(val);
                            }catch (Exception e){
                                return false;
                            }
                        }else if(type.equals("DATE")){
                            if (!isData(val.substring(1, val.length()-1)))
                                return false;
                        }else {
                            if (val.length() > Integer.parseInt(column.getTypeLength())){
                                return false;
                            }
                        }
                        try {
                            if(!column.getCheck().equals("null")){
                                String[] ss = column.getCheck().split(" ");
                                if(ss[1].equals(">")){
                                    if(type.equals("INTEGER")){
                                        try {
                                            int left = Integer.parseInt(val);
                                            int right = Integer.parseInt(ss[2]);
                                            if (left <= right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else if(type.equals("DOUBLE")){
                                        try {
                                            double left = Double.parseDouble(val);
                                            double right = Double.parseDouble(ss[2]);
                                            if (left <= right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        return false;
                                    }
                                }else if(ss[1].equals("<")){
                                    if(type.equals("INTEGER")){
                                        try {
                                            int left = Integer.parseInt(val);
                                            int right = Integer.parseInt(ss[2]);
                                            if (left >= right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else if(type.equals("DOUBLE")){
                                        try {
                                            double left = Double.parseDouble(val);
                                            double right = Double.parseDouble(ss[2]);
                                            if (left >= right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        return false;
                                    }
                                }else if(ss[1].equals("<=")){
                                    if(type.equals("INTEGER")){
                                        try {
                                            int left = Integer.parseInt(val);
                                            int right = Integer.parseInt(ss[2]);
                                            if (left > right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else if(type.equals("DOUBLE")){
                                        try {
                                            double left = Double.parseDouble(val);
                                            double right = Double.parseDouble(ss[2]);
                                            if (left > right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        return false;
                                    }
                                }else if(ss[1].equals(">=")){
                                    if(type.equals("INTEGER")){
                                        try {
                                            int left = Integer.parseInt(val);
                                            int right = Integer.parseInt(ss[2]);
                                            if (left < right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else if(type.equals("DOUBLE")){
                                        try {
                                            double left = Double.parseDouble(val);
                                            double right = Double.parseDouble(ss[2]);
                                            if (left < right){
                                                return false;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }else {
                                        return false;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            return false;
                        }
                        size--;
                    }
                }
                System.out.println(size);
                if (size == 0){
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean isData(String str){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = null;
        try {
            date = dateFormat.parse(str);
            System.out.println(date.toLocaleString().split(" ")[0]);//切割掉不要的时分秒数据
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean DataSave(HashMap<String,String> map){
        try{
            File file = new File("src\\DBMS_ROOT\\data\\" + dbName + "/" + tbName + ".ibd");
            FileInputStream input = new FileInputStream(file);
            byte[] buffer = new byte[10240];
            input.read(buffer);
            input.close();
            DBMS.Table table = SerializationUtils.deserialize(buffer);
            List<DBMS.Table.Column> columnList = new ArrayList<>(table.getColumnList());
            List<DBMS.Table.Column> columnList1 = new ArrayList<>();
            for (DBMS.Table.Column column : columnList){
                if (map.containsKey(column.getColumnName())){
                    List<String> val = new ArrayList<>(column.getValList());
                    val.add(map.get(column.getColumnName()));
                    DBMS.Table.Column column1 = DBMS.Table.Column
                            .newBuilder()
                            .setColumnName(column.getColumnName())
                            .setType(column.getType())
                            .setTypeLength(column.getTypeLength())
                            .setComment(column.getComment())
                            .setCheck(column.getCheck())
                            .setDefault(column.getDefault())
                            .setIdentity(column.getIdentity())
                            .addAllVal(val)
                            .setPrimary(column.getPrimary())
                            .setForeign(column.getForeign())
                            .setNotNull(column.getNotNull())
                            .setUnique(column.getUnique())
                            .build();
                    columnList1.add(column1);
                }else {
                    List<String> val = new ArrayList<>(column.getValList());
                    if (!column.getDefault().equals("null"))
                        val.add(map.get(column.getDefault()));
                    else {
                        String s = "null";
                        val.add(s);
                    }
                    DBMS.Table.Column column1 = DBMS.Table.Column
                            .newBuilder()
                            .setColumnName(column.getColumnName())
                            .setType(column.getType())
                            .setTypeLength(column.getTypeLength())
                            .setComment(column.getComment())
                            .setCheck(column.getCheck())
                            .setDefault(column.getDefault())
                            .setIdentity(column.getIdentity())
                            .addAllVal(val)
                            .setPrimary(column.getPrimary())
                            .setForeign(column.getForeign())
                            .setNotNull(column.getNotNull())
                            .setUnique(column.getUnique())
                            .build();
                    columnList1.add(column1);
                }
            }
            DBMS.Table table1 = DBMS.Table.newBuilder()
                    .setTableName(table.getTableName())
                    .addAllColumn(columnList1)
                    .build();
            FileOutputStream fileOutputStream = new FileOutputStream("src\\DBMS_ROOT\\data\\"+ dbName +"\\"+tbName+".ibd");
            byte[] val = SerializationUtils.serialize(table1);
            for(int i = 0; i < val.length;i++){
                fileOutputStream.write(val[i]);
            }
            fileOutputStream.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
