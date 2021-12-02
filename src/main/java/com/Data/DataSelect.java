package com.Data;

import com.DBMS.proto.DBMS;
import org.apache.commons.lang3.SerializationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class DataSelect {
    public String tbName;
    public String dbName;

    public DataSelect(String dbName,String tbName){
        this.dbName = dbName;
        this.tbName = tbName;
    }

    public String select(List<String> result ,List<String> key ,List<String> val ){
        try {
            File file = new File("src\\DBMS_ROOT\\data\\"+dbName+"\\"+tbName+".ibd");
            if (file.exists()) {
                FileInputStream input = new FileInputStream(file);
                byte[] buffer = new byte[10240];
                input.read(buffer);
                input.close();
                DBMS.Table table = SerializationUtils.deserialize(buffer);
                List<DBMS.Table.Column> columnList = new ArrayList<>(table.getColumnList());
                HashSet<Integer> set = new HashSet<>();
                HashMap<Integer, String> map = new HashMap<>();
                for (int i = 0; i < columnList.get(0).getValList().size(); i++) {
                    set.add(i);
                }
                int m = 0;
                for(int i = 0;i<key.size();i ++){
                    for(int j = 0;j < columnList.size();j++){
                        if (key.get(i).equals(columnList.get(j).getColumnName())){
                            HashSet<Integer> set1 = new HashSet<>();
                            List<String> list = new ArrayList<>(columnList.get(j).getValList());
                            for (int z = 0; z<list.size();z++){
                                if(list.get(z).equals(val.get(i))){
                                    set1.add(z);
                                }
                            }
                            set.retainAll(set1);
                            m++;
                        }
                    }
                }
                if(m != key.size()||set.size() == 0)
                    return "null";
                HashMap<String,List<String>> results = new HashMap<>();
                int j = 0;
                if(result.get(0).equals("*")&&result.size() == 1){
                    for (DBMS.Table.Column column: columnList){
                        List<String> list = new ArrayList<>(column.getValList());
                        List<String> list1 = new ArrayList<>();
                        for (Integer i : set){
                            list1.add(list.get(i));
                        }
                        results.put(column.getColumnName(),list1);
                    }
                    String allKeys = "";
                    String allValues = "";
                    List<List<String>> aa = new ArrayList<>();;
                    for (Map.Entry<String,List<String>> entry:results.entrySet()){
                        String currentKey = "|" + entry.getKey();

                        System.out.println("KEY:"+entry.getKey());

                        currentKey = String.format("%-15s", currentKey);
                        allKeys += currentKey;
                        System.out.println("VALUE:"+entry.getValue());
                        aa.add(entry.getValue());
                    }
                    for(int i = 0; i < aa.get(0).size(); i++){
                        for(int k = 0; k < aa.size(); k++){
                            String currentValue =  "|" + aa.get(k).get(i) ;
                            currentValue = String.format("%-15s", currentValue);
                            allValues += currentValue;
                        }
                        allValues += "\n";
                    }

                    return allKeys + "\n" + allValues;
                }else {
                    for (DBMS.Table.Column column: columnList){
                        for (String res : result){
                            if(column.getColumnName().equals(res)){
                                List<String> list = new ArrayList<>(column.getValList());
                                List<String> list1 = new ArrayList<>();
                                for (Integer i : set){
                                    list1.add(list.get(i));
                                }
                                results.put(column.getColumnName(),list1);
                            }
                            j++;
                        }
                    }
                    if(j == result.size()) {
                        String allKeys = "";
                        String allValues = "";
                        List<List<String>> aa = new ArrayList<>();;
                        for (Map.Entry<String,List<String>> entry:results.entrySet()){
                            String currentKey = "|" + entry.getKey();

                            System.out.println("KEY:"+entry.getKey());

                            currentKey = String.format("%-15s", currentKey);
                            allKeys += currentKey;
                            System.out.println("VALUE:"+entry.getValue());
                            aa.add(entry.getValue());
                        }
                        for(int i = 0; i < aa.get(0).size(); i++){
                            for(int k = 0; k < aa.size(); k++){
                                String currentValue =  "|" + aa.get(k).get(i) ;
                                currentValue = String.format("%-15s", currentValue);
                                allValues += currentValue;
                            }
                            allValues += "\n";
                        }


                        return allKeys + "\n" + allValues;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "null";
    }
}
