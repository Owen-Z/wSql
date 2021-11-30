package API;
import com.Database.DatabaseCreate;
import com.Database.DatabaseDelete;
import com.Table.TableCreate;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.*;
import com.alibaba.druid.sql.dialect.mysql.ast.clause.*;
import com.alibaba.druid.sql.dialect.mysql.ast.expr.*;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitor;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlASTVisitorAdapter;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerInsertStatement;
import com.alibaba.druid.sql.dialect.sqlserver.ast.stmt.SQLServerUpdateStatement;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.util.JdbcConstants;
import jdk.swing.interop.SwingInterOpUtils;

import java.io.*;
import java.util.*;

public class API {
    private String userName;    //使用当前模块的用户名
    private String dbName;      //当前使用的数据库

    public API(){
//        userName = un;
//        dbName = dbn;
    }

    public String getDbName() {
        return dbName;
    }

    public String getUserName() {
        return userName;
    }

    /**
     * 删除指定数据库
     *
     * @param  dbName, filePath
     */
    public void DeleteDatabase(String dbName, String filePath){
        try{

            File file = new File(filePath+"\\"+dbName);
            System.out.println(filePath+"\\"+dbName);
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断数据库名或者表名是否重复
     *
     * 重复则返回true, 不重复则返回false
     * @param str, filePath
     * @return exist
     */
    public boolean CheckExist(String str, String filePath){
        boolean exist = false;  // 表示是否重复的BOOLEAN变量

        // 读取指定文件，并比较每一行数据判断是否出现重复
        try{
            File file = new File(filePath);
            if(file.exists()){
                File[] fileArray= file.listFiles();
                for(int i = 0;i<fileArray.length;i++){
                    if(fileArray[i].getName().equals(str)){
                        exist = true;
                        break;
                    }
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        return exist;
    }

    public void parse(String statement) {
        // 使用druid解析语句
        // 第一个参数为SQL语句
        // 第二个参数为解析的数据库类型
        statement = statement.toUpperCase(Locale.ROOT);
        List<SQLStatement> statementList = SQLUtils.parseStatements(statement, JdbcConstants.MYSQL);
        // 单语句解析，只有一条数据
        if (!statement.isEmpty()) {
            SQLStatement sqlStatement = statementList.get(0);
            // 插入语句解析

            // 创建数据库语句
            if (sqlStatement instanceof SQLCreateDatabaseStatement) {
                // 转换语句格式
                SQLCreateDatabaseStatement createDatabaseStatement = (SQLCreateDatabaseStatement) sqlStatement;

                // 通过解析得到的数据库名含有''   e.g. 'test'
                String dbName = createDatabaseStatement.getName().toString();
                // 去除了''     e.g. test
                String storeDBName = dbName.substring(1, dbName.length() - 1);

                DatabaseCreate databaseCreate = new DatabaseCreate();
                if(databaseCreate.DBCheck(storeDBName) == 0){
                    System.out.println("数据库名称过长");
                }else if(databaseCreate.DBCheck(storeDBName) == 1){
                    System.out.println("数据库已存在");
                }else if(databaseCreate.DBCheck(storeDBName) == 2){
                    databaseCreate.DBCreate(storeDBName);
                    System.out.println("数据库创建成功");
                }

            }

            // 删除数据库指令
            if (sqlStatement instanceof SQLDropDatabaseStatement) {
                // 转换语句格式
                SQLDropDatabaseStatement sqlDropDatabaseStatement = (SQLDropDatabaseStatement) sqlStatement;

                // 通过解析得到的数据库名含有''   e.g. 'test'
                String dbName = sqlDropDatabaseStatement.getDatabase().toString();
                // 去除了''     e.g. test
                String storeDBName = dbName.substring(1, dbName.length() - 1);

                /*
                    判断数据库是否存在
                    存在则删除该数据库
                    否则提示该数据库不存在
                 */
                DatabaseDelete databaseDelete = new DatabaseDelete();
                if (!databaseDelete.DBCheck(storeDBName)) {
                    System.out.println("数据库不存在");
                } else {
                    databaseDelete.BDDelete(storeDBName);
                    System.out.println("数据库删除成功");
                }

            }

            // 创建表指令
            if (sqlStatement instanceof SQLCreateTableStatement) {
                SQLCreateTableStatement sqlCreateTableStatement = (SQLCreateTableStatement) sqlStatement;
                System.out.println(sqlCreateTableStatement);

                // 表名 e.g. 'test'
                String tableName = sqlCreateTableStatement.getName().toString();
                // 用以存储的表名 e.g. test
                String storeTableName = tableName.substring(1, tableName.length() - 1);

                //实例化创建表
                TableCreate tableCreate = new TableCreate("MYSQLITE",storeTableName);
                if (tableCreate.TBCheck()){
                    System.out.println("表已经存在");
                    return;
                }

                List<SQLTableElement> elements = sqlCreateTableStatement.getTableElementList();

                for (int i = 0; i < elements.size(); i++) {
                    if (elements.get(i) instanceof SQLColumnDefinition) {
                        HashMap<String, String> map = new HashMap<>();
                        //1.name
                        String name = ((SQLColumnDefinition) elements.get(i)).getNameAsString();
                        map.put("name", name);
                        //2.type
                        String type = ((SQLColumnDefinition) elements.get(i)).getDataType().toString();
                        map.put("type", type);
                        //3.def
                        String def = "null";
                        if (((SQLColumnDefinition) elements.get(i)).getDefaultExpr() != null) {
                            def = ((SQLColumnDefinition) elements.get(i)).getDefaultExpr().toString();
                        }
                        map.put("def", def);
                        //4.comment
                        String comment = "null";
                        if (((SQLColumnDefinition) elements.get(i)).getComment() != null) {
                            comment = ((SQLColumnDefinition) elements.get(i)).getDefaultExpr().toString();
                        }
                        map.put("comment", comment);
                        //5.auto
                        String auto = "false";
                        if (((SQLColumnDefinition) elements.get(i)).isAutoIncrement()) {
                            auto = "true";
                        }
                        map.put("auto", auto);
                        //7.8.9.10
                        List<SQLColumnConstraint> list =
                                ((SQLColumnDefinition) elements.get(i)).getConstraints();
                        String primaryKey = "false";
                        String notNUll = "false";
                        String check = "null";
                        String foreignKey = "false";
                        String unique = "false";
                        for (SQLColumnConstraint cons : list) {
                            if (cons instanceof SQLNotNullConstraint) {
                                notNUll = "true";
                            }
                            if (cons instanceof SQLColumnPrimaryKey) {
                                primaryKey = "true";
                            }
                            if (cons instanceof SQLColumnCheck) {
                                check = ((SQLColumnCheck) cons).getExpr().toString();
                            }
                            if (cons instanceof SQLColumnUniqueKey) {
                                unique = "true";
                            }
                        }
                        map.put("notNUll", notNUll);
                        map.put("primaryKey", primaryKey);
                        map.put("check", check);
                        map.put("foreignKey", foreignKey);
                        map.put("unique", unique);
//                        System.out.println(i + "name:" + name);
//                        System.out.println(i + "type:" + type);
//                        System.out.println(i + "def:" + def);
//                        System.out.println(i + "comment:" + comment);
//                        System.out.println(i + "auto:" + auto);
//                        System.out.println(i + "primaryKey:" + primaryKey);
//                        System.out.println(i + "notNUll:" + notNUll);
//                        System.out.println(i + "check:" + check);
//                        System.out.println(i + "unique:" + unique);
//                        System.out.println(i + "foreignKey:" + foreignKey);
                        tableCreate.TBCreateColumn(map);
                    } else if (elements.get(i) instanceof MySqlPrimaryKey) {
                        MySqlSchemaStatVisitor visitor1 = new MySqlSchemaStatVisitor();
                        elements.get(i).accept(visitor1);
                        String[] str = visitor1.getColumns().toString().split("\\[|\\.|\\]");
//                        System.out.println(str[2]);
                        if(tableCreate.setPrimaryKey(str[2])){
                            System.out.println("主键设置成功");
                        }else{
                            System.out.println("主键已经存在");
                        }
                    } else if (elements.get(i) instanceof MysqlForeignKey) {
                        MySqlSchemaStatVisitor visitor1 = new MySqlSchemaStatVisitor();
                        elements.get(i).accept(visitor1);
                        String str = visitor1.getColumns().toString();
                        String result = str.substring(1,str.length()-1);
                        String[] s = result.split(" ");
                        String[] s1 = s[0].split("\\.|\\,");
                        String[] s2 = s[1].split("\\.");
//                        System.out.println(s1[1]);
//                        System.out.println(s2[0]);
//                        System.out.println(s2[1]);
                        int result1 = tableCreate.setForeign(s1[1],s2[0],s2[1]);
                        if(result1 == 0){
                            System.out.println("字段不存在");
                        }else if(result1 == 1){
                            System.out.println("外键不存在");
                        }else if(result1 == 2){
                            System.out.println("字段类型不匹配");
                        }else if(result1 == 3){
                            System.out.println("字段类型不适合");
                        }else if(result1 == 4){
                            System.out.println("外键设置成功");
                        }
                    }
                }
                tableCreate.create();
                // 普通约束
//                System.out.println(sqlCreateTableStatement.getTableElementList());

//                MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
//                ((MysqlForeignKey)element).accept(visitor);
//                System.out.println(visitor.getColumns());
//                System.out.println(((MysqlForeignKey)element).getText());

//                System.out.println(((MySqlPrimaryKey)element).getName());
//                MySqlSchemaStatVisitor visitor1 = new MySqlSchemaStatVisitor();
//                element.accept(visitor1);
//                String[] str = visitor1.getColumns().toString().split("\\[|\\.|\\]");
//                System.out.println(str[2]);

            }

            // 删除数据表
            if(sqlStatement instanceof SQLDropTableStatement){
                SQLDropTableStatement sqlDropTableStatement = (SQLDropTableStatement) sqlStatement;
                System.out.println(sqlDropTableStatement.getTableSources().toString());
            }

            // 修改数据表字段
            if(sqlStatement instanceof SQLAlterTableStatement){
                // 转换
                SQLAlterTableStatement sqlAlterTableStatement = (SQLAlterTableStatement) sqlStatement;
//                System.out.println(sqlAlterTableStatement.getItems().size());

                System.out.println(sqlAlterTableStatement.getTableSource());

                for(SQLAlterTableItem element : sqlAlterTableStatement.getItems()){
                    if(element instanceof SQLAlterTableDropColumnItem){
                        System.out.println(((SQLAlterTableDropColumnItem) element).getColumns());
                        for(SQLName dropElement : ((SQLAlterTableDropColumnItem) element).getColumns()){
                            System.out.println(dropElement);
                        }
                    }else if(element instanceof SQLAlterTableAddColumn){
                        SQLColumnDefinition addElement = ((SQLAlterTableAddColumn) element).getColumns().get(0);
                        System.out.println(addElement.getName());
                        System.out.println(addElement.getDataType());
                        // 同创建表约束
                        System.out.println(addElement.getConstraints());
                    }
                }


            }

            // 更新数据表数据
            if(sqlStatement instanceof SQLUpdateStatement){
                System.out.println(6666);
                SQLUpdateStatement sqlUpdateStatement = (SQLUpdateStatement) sqlStatement;
                // 字段需要变成什么样
                System.out.println(sqlUpdateStatement.getItems().getClass());
                for(SQLUpdateSetItem item : sqlUpdateStatement.getItems()){
                    // e.g. student.id = 19
                    // 需要设置的字段
                    System.out.println(item.getColumn());   //student.id
                    // 字段需要设置成的值
                    System.out.println(item.getValue());    //19
                }


                // where条件
                SQLBinaryOpExpr where= (SQLBinaryOpExpr) sqlUpdateStatement.getWhere();

                while (where.getOperator().toString().equals("BooleanAnd")){
                    System.out.println(((SQLBinaryOpExpr)where.getRight()).getRight());
                    System.out.println(((SQLBinaryOpExpr)where.getRight()).getLeft());
                    where = (SQLBinaryOpExpr)where.getLeft();
                }
                System.out.println(where.getRight());
                System.out.println(where.getLeft());
//                while (where.getOperator() instanceof SQLBinaryOpExpr)
//                System.out.println(sqlUpdateStatement.getWhere());
            }




            if (sqlStatement instanceof SQLSelectStatement) {
                // 转换
                SQLSelectStatement sqlSelectStatement = (SQLSelectStatement) sqlStatement;
//                System.out.println(sqlSelectStatement);


                MySqlASTVisitor visitor = new MySqlASTVisitorAdapter();
                MySqlSchemaStatVisitor visitor1 = new MySqlSchemaStatVisitor();
                sqlSelectStatement.accept(visitor1);

                System.out.println(visitor1.getTables().getClass());

                
                int needReturn = visitor1.getColumns().size() - visitor1.getConditions().size();
                int length = visitor1.getColumns().toString().length();
                String str = visitor1.getColumns().toString().substring(1,length-1);
                String[] sList = str.split(",");
                for(int i = 0; i < needReturn; i++){

                    System.out.println(sList[i]);
                }
                // 获取所有的普通值限制
                List<TableStat.Condition> conditions = visitor1.getConditions();
                for (TableStat.Condition condition : conditions) {
                    if (condition.getValues().size() != 0) {
                        System.out.println(condition.getColumn());  // 需要查的表名+列名
                        System.out.println(condition.getValues().get(0));   // 对应的值
                    }
                }

                // 连续查询两个表
                if (visitor1.getTables().size() > 1) {
                    // 两个表有连接查询
                    if (visitor1.getRelationships().size() > 0) {
                        System.out.println(visitor1.getRelationships());    //  两个表的表名.列名 = 表名.列名
                    }
                }


//                MySqlSchemaStatVisitor visitor = new MySqlSchemaStatVisitor();
                sqlSelectStatement.accept(visitor);
//                System.out.println(visitor.visit(sqlSelectStatement));
                SQLSelectQuery sqlSelectQuery = sqlSelectStatement.getSelect().getQuery();

//                System.out.println(sqlSelectStatement.getSelect());
//                System.out.println(sqlSelectStatement.getSelect().getQueryBlock()); // whole query command


//                System.out.println(sqlSelectStatement.getSelect().getQueryBlock().getWhere().getClass());
//                SQLBinaryOpExpr expr = (SQLBinaryOpExpr) sqlSelectStatement.getSelect().getQueryBlock().getWhere();
//                System.out.println(expr);



                /*
                    首先判断是否为普通表 instanceof SQLExprTableSource
                    否则是SQLJoinTableSource 有左值，右值
                    设置原表名和表名的hashmap
                 */
//                SQLJoinTableSource from = (SQLJoinTableSource) sqlSelectStatement.getSelect().getQueryBlock().getFrom();
//                String[] names = from.toString().split(",");
//                HashMap<String, String > name_RName = new HashMap<>();
//                for(int i = names.length - 1; i > 1; i--){
//                   String str = from.getRight().getAlias();
//                   if(str != null){
//                       name_RName.put(names[i], str);
//                   }else{
//                       name_RName.put(names[i], names[i]);
//                   }
//                   from = (SQLJoinTableSource) from.getLeft();
//                }
//                String rstr = from.getRight().getAlias();
//                String lstr = from.getLeft().getAlias();
//                if(rstr != null){
//                    name_RName.put(names[1], rstr);
//                }else{
//                    name_RName.put(names[1], names[1]);
//                }
//                if(lstr != null){
//                    name_RName.put(names[0], lstr);
//                }else{
//                    name_RName.put(names[0], names[0]);
//                }


//                System.out.println(from.getRight().getAlias());  //获取别名


//                System.out.println(sqlSelectStatement.getSelect().getQueryBlock().getGroupBy());
//                System.out.println(sqlSelectStatement.getSelect().getQueryBlock().getOrderBy());
//                System.out.println(sqlSelectStatement.getSelect().getQueryBlock().getLimit());

            }

        }

    }

}
