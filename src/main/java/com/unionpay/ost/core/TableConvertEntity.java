package com.unionpay.ost.core;

import com.unionpay.ost.bean.EntityMeta;
import com.unionpay.ost.bean.FieldMeta;
import com.unionpay.ost.bean.SequenceMeta;
import com.unionpay.ost.config.Configuration;
import com.unionpay.ost.config.DataBaseRelateConstant;
import com.unionpay.ost.exception.MyException;
import com.unionpay.ost.table.ColumnInfo;
import com.unionpay.ost.table.TableInfo;
import com.unionpay.ost.utils.DatabaseTypeConvertUtil;
import com.unionpay.ost.utils.JDBCUtils;
import com.unionpay.ost.utils.StringUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 将表信息转换为对应的实体类信息
 * Created by jsf on 16/8/6..
 */
public class TableConvertEntity {


    /**
     * 根据不同的数据库类型将数据库对应的数据表转化为实体类对象
     *
     * @return 实体类对象集合
     */
    public static List<EntityMeta> accordDBTypeObtainEntityMetas() {
        List<EntityMeta> entityMetaList = new ArrayList<EntityMeta>();
        String jdbcUrl = Configuration.getJdbcUrl();
        if (jdbcUrl.contains(DataBaseRelateConstant.MYSQL)) {
            entityMetaList = obtainEntityMetas(DataBaseRelateConstant.MYSQL);
        } else if (jdbcUrl.contains(DataBaseRelateConstant.DB2)) {
            entityMetaList = obtainEntityMetas(DataBaseRelateConstant.DB2);
        }
        return entityMetaList;
    }

    /**
     * 将数据库对应的数据表转化为实体类对象
     *
     * @return 实体类对象集合
     */
    private static List<EntityMeta> obtainEntityMetas(String dbType) {
        Connection connection = DataBaseManager.openConnection();
        System.out.println("已经连接到"+dbType+"数据库,开始生成数据库表对应的entity类");
        PreparedStatement ps = null;
        ResultSet rs = null;
        String tableName = null;
        List<EntityMeta> entityMetaList = new ArrayList<EntityMeta>();

        try {
            //如果没有配置数据库表,则执行该段代码
            if(StringUtils.isStrArrayEmpty(Configuration.getTableNames())){
                if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                    ps = connection.prepareStatement("show tables from " + Configuration.getSchema());
                } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                    ps = connection.prepareStatement("select * from syscat.tables where tabschema=?");
                    ps.setString(1, Configuration.getSchema());
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    EntityMeta entityMeta = new EntityMeta();
                    TableInfo tableInfo = new TableInfo();
                    if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                        tableName = rs.getString("Tables_in_" + Configuration.getSchema());
                    } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                        tableName = rs.getString("TABNAME");
                    }
                    //这里主要用到是数据表名
                    tableInfo.setTableName(tableName);
                    //组装表实体对象模型
                    entityMeta.setEntityName(StringUtils.capInMark(tableName, DataBaseRelateConstant.tableSpiltMark, true));
                    if (DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)) {
                        entityMeta.setFieldMetaList(obtainFieldMetasForMySQL(connection, tableName));
                    } else if (DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)) {
                        entityMeta.setFieldMetaList(obtainFieldMetasForDB2(connection, tableName));
                    }
                    entityMeta.setTableInfo(tableInfo);
                    entityMetaList.add(entityMeta);
                }
            }else{
                //如果配置数据库表,则执行该段代码
                for(String tabName:Configuration.getTableNames()){
                     if(DataBaseRelateConstant.MYSQL.equalsIgnoreCase(dbType)){
                         EntityMeta entityMeta = new EntityMeta();
                         TableInfo tableInfo = new TableInfo();
                         //这里主要用到是数据表名
                         tableInfo.setTableName(tabName);
                         //组装表实体对象模型
                         entityMeta.setEntityName(StringUtils.capInMark(tabName, DataBaseRelateConstant.tableSpiltMark, true));
                         entityMeta.setFieldMetaList(obtainFieldMetasForMySQL(connection, tabName));
                         entityMeta.setTableInfo(tableInfo);
                         entityMetaList.add(entityMeta);
                     }else if(DataBaseRelateConstant.DB2.equalsIgnoreCase(dbType)){
                         EntityMeta entityMeta = new EntityMeta();
                         TableInfo tableInfo = new TableInfo();
                         //这里主要用到是数据表名
                         tableInfo.setTableName(tabName);
                         //组装表实体对象模型
                         entityMeta.setEntityName(StringUtils.capInMark(tabName, DataBaseRelateConstant.tableSpiltMark, true));
                         entityMeta.setFieldMetaList(obtainFieldMetasForDB2(connection, tabName));
                         entityMeta.setTableInfo(tableInfo);
                         entityMetaList.add(entityMeta);
                     }
                }
            }

        } catch (SQLException e) {
            throw new MyException(e, "无法创建statement对象");
        } finally {
            //关闭相关资源
            JDBCUtils.shutDownDataBaseResource(rs, ps, connection);
        }
        return entityMetaList;
    }

    /**
     * 获取每个数据库表中数据字段的信息
     *
     * @param connection 数据库连接
     * @param tableName  数据库中的表名
     * @return 数据表字段对应的属性对象的集合
     */
    private static List<FieldMeta> obtainFieldMetasForMySQL(Connection connection, String tableName) {
        List<FieldMeta> fieldMetaList = new ArrayList<FieldMeta>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            String columnInfoSQL = "select COLUMN_NAME,DATA_TYPE,COLUMN_TYPE,COLUMN_KEY,IS_NULLABLE,COLUMN_COMMENT,NUMERIC_PRECISION,NUMERIC_SCALE,DATETIME_PRECISION from information_schema.COLUMNS " +
                    "where TABLE_SCHEMA=? AND TABLE_NAME=? ORDER BY ORDINAL_POSITION";
            ps = connection.prepareStatement(columnInfoSQL);
            ps.setString(1, Configuration.getSchema());
            ps.setString(2, tableName);
            rs = ps.executeQuery();
            while (rs.next()) {
                FieldMeta fieldMeta = new FieldMeta();
                ColumnInfo columnInfo = new ColumnInfo();
                String columnName = rs.getString("COLUMN_NAME");
                String columnDateType = rs.getString("DATA_TYPE");
                String columnType = rs.getString("COLUMN_TYPE");
                String columnKey = rs.getString("COLUMN_KEY");
                String columnIsNull = rs.getString("IS_NULLABLE");
                String columnComment = rs.getString("COLUMN_COMMENT");
                String columnPrecision = rs.getString("NUMERIC_PRECISION");
                String columnScale = rs.getString("NUMERIC_SCALE");
                String columnDateTimePrecision = rs.getString("DATETIME_PRECISION");
                //将相关的结果信息存储到列信息中(这里主要用到的是字段名)
                columnInfo.setColumnName(columnName);
                //将相关的结果集放入到Field对象中;
                fieldMeta.setFieldName(StringUtils.capInMark(columnName, DataBaseRelateConstant.tableSpiltMark, false));
                fieldMeta.setFieldType(DatabaseTypeConvertUtil.dataBaseTypeToJavaTypeForMYSQL(columnDateType.toUpperCase()));
                if (!columnDateType.equals(columnType)) {
                    //对整数或带有精度的时间类型进行处理
                    if (StringUtils.isEmpty(columnScale) || (0 == Long.parseLong(columnScale))) {
                        long columnLength = Long.parseLong(columnType.replace("(", "").replace(")", "").replace(columnDateType, ""));
                        fieldMeta.setFieldLength(columnLength);
                        if (!StringUtils.isEmpty(columnDateTimePrecision)) {
                            fieldMeta.setFieldDateTimePrecision(Long.parseLong(columnDateTimePrecision));
                        }
                    } else {
                        //对带有小数精度要求的数进行处理
                        fieldMeta.setFieldPrecision(Long.parseLong(columnPrecision));
                        fieldMeta.setFieldScale(Long.parseLong(columnScale));
                    }
                }
                //对于MySQl数据库主键的标识是以"PRI"认知的
                if (columnKey.contains("PRI")) {
                    fieldMeta.setWhetherPK(true);
                }
                //该列是否为空,存放的是YES或NO
                if ("YES".equalsIgnoreCase(columnIsNull)) {
                    fieldMeta.setWhetherNULL(true);
                }
                //字段的注释
                if(!StringUtils.isEmpty(columnComment)){
                    fieldMeta.setFieldComment(columnComment);
                }
                fieldMeta.setColumnInfo(columnInfo);
                fieldMetaList.add(fieldMeta);
            }

        } catch (SQLException e) {
            throw new MyException(e, "MYSQL无法执行该查询列的SQL语句");
        } finally {
            //关闭相关资源
            JDBCUtils.shutDownDataBaseResource(rs, ps, connection, false);
        }
        return fieldMetaList;
    }

    private static List<FieldMeta> obtainFieldMetasForDB2(Connection connection, String tableName) {
        List<FieldMeta> fieldMetaList = new ArrayList<FieldMeta>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        //DB2数据库相关的表的字段的信息
        String queryUnionSql = "FROM SYSCAT.COLUMNS C  " +
                "LEFT OUTER JOIN SYSCAT.KEYCOLUSE KCU ON KCU.TABSCHEMA = C.TABSCHEMA AND KCU.TABNAME = C.TABNAME  AND KCU.COLNAME = C.COLNAME  " +
                "LEFT OUTER JOIN SYSCAT.TABCONST TC ON TC.CONSTNAME = KCU.CONSTNAME AND KCU.TABSCHEMA=TC.TABSCHEMA AND KCU.TABNAME=TC.TABNAME " +
                "LEFT OUTER JOIN SYSCAT.COLIDENTATTRIBUTES CID ON CID.COLNAME = C.COLNAME AND CID.TABNAME = C.TABNAME AND CID.TABSCHEMA = C.TABSCHEMA  " +
                "LEFT OUTER JOIN SYSCAT.SEQUENCES SEQ ON CID.SEQID=SEQ.SEQID  " +
                "LEFT OUTER JOIN SYSIBM.COLUMNS IBMC ON IBMC.TABLE_NAME=C.TABNAME AND IBMC.COLUMN_NAME=C.COLNAME AND IBMC.TABLE_SCHEMA=C.TABSCHEMA";

        String columnDetailInfoSQL = "SELECT C.TABNAME AS TABNAME,C.COLNAME AS COLNAME,C.REMARKS AS COMMENT, TC.TYPE AS ISPRIM,C.TYPENAME AS TYPENAME," +
                " C.LENGTH AS LENGTH,C.SCALE AS SCALE,IBMC.NUMERIC_PRECISION AS NUMERIC_PRECISION,IBMC.NUMERIC_SCALE AS NUMERIC_SCALE,IBMC.DATETIME_PRECISION AS DATETIME_PRECISION," +
                "C.SCALE AS SCALE,IBMC.IS_NULLABLE AS IS_NULLABLE,C.DEFAULT AS DEFAULT,C.TEXT AS TEXT, CID.START AS START,CID.INCREMENT AS INCREMENT,SEQ.SEQNAME AS SEQNAME  "
                + queryUnionSql + " WHERE C.TABNAME=? and C.TABSCHEMA=? ORDER BY COLNO FOR FETCH ONLY ";
        try {
            ps = connection.prepareStatement(columnDetailInfoSQL);
            ps.setString(1, tableName);
            ps.setString(2, Configuration.getSchema());
            rs = ps.executeQuery();
            while (rs.next()) {
                FieldMeta fieldMeta = new FieldMeta();
                ColumnInfo columnInfo = new ColumnInfo();
                SequenceMeta sequenceMeta = new SequenceMeta();
                String columnName = rs.getString("COLNAME");
                String columnDateType = rs.getString("TYPENAME");
                String columnKey = rs.getString("ISPRIM");
                String columnIsNull = rs.getString("IS_NULLABLE");
                String columnComment = rs.getString("COMMENT");
                String columnLength = rs.getString("LENGTH");
                String columnPrecision = rs.getString("NUMERIC_PRECISION");
                String columnScale = rs.getString("NUMERIC_SCALE");
                String columnDateTimePrecision = rs.getString("DATETIME_PRECISION");
                String sequenceStart = rs.getString("START");
                String sequenceIncrement = rs.getString("INCREMENT");
                String sequenceName = rs.getString("SEQNAME");
                //构造序列对象
                if (!StringUtils.isEmpty(sequenceName)) {
                    sequenceMeta.setSequenceName(sequenceName);
                    sequenceMeta.setSequenceStrategy(SequenceMeta.SEQUENCE_STRATEGY);
                    sequenceMeta.setSequenceAllocationSize(Long.parseLong(sequenceIncrement));
                    sequenceMeta.setSequenceInitialValue(Long.parseLong(sequenceStart));
                } else {
                    sequenceMeta = null;
                }
                //将相关的结果信息存储到列信息中(这里主要用到的是字段名)
                columnInfo.setColumnName(columnName);
                //将相关的结果集放入到Field对象中;
                fieldMeta.setFieldName(StringUtils.capInMark(columnName, "_", false));
                fieldMeta.setFieldType(DatabaseTypeConvertUtil.dataBaseTypeToJavaTypeForDB2(columnDateType.toUpperCase()));
                //由于db2数据库中,如果小数精度类型为0,那么和整数无法在字段上区别,同时在entity类上显示注解有区别,只有通过类型来判断是整数还是带有精度数
                if (!StringUtils.isEmpty(columnPrecision)) {
                    fieldMeta.setFieldPrecision(Long.parseLong(columnPrecision));
                    if (!StringUtils.isEmpty(columnScale)) {
                        fieldMeta.setFieldScale(Long.parseLong(columnScale));
                    }
                    //如果是这三种数据类型,则将Precision置为null
                    String[] wholeNumberType = {"INTEGER", "BIGINT", "SMALLINT"};
                    for (String strType : wholeNumberType) {
                        if (strType.equalsIgnoreCase(columnDateType)) {
                            fieldMeta.setFieldLength(Long.parseLong(columnLength));
                            fieldMeta.setFieldPrecision(null);
                            break;
                        }
                    }
                } else {
                    //可能是字符型也可能是日期类型
                    fieldMeta.setFieldLength(Long.parseLong(columnLength));
                    if (!StringUtils.isEmpty(columnDateTimePrecision)) {
                        fieldMeta.setFieldDateTimePrecision(Long.parseLong(columnDateTimePrecision));
                    }
                }
                //对于DB2数据库主键的标识是以"P"认知的
                if ("P".equalsIgnoreCase(columnKey)) {
                    fieldMeta.setWhetherPK(true);
                }
                //该列是否为空,存放的是YES或NO
                if ("YES".equalsIgnoreCase(columnIsNull)) {
                    fieldMeta.setWhetherNULL(true);
                }
                //字段的注释
                if(!StringUtils.isEmpty(columnComment)){
                    fieldMeta.setFieldComment(columnComment);
                }
                fieldMeta.setSequenceMeta(sequenceMeta);
                fieldMeta.setColumnInfo(columnInfo);
                fieldMetaList.add(fieldMeta);
            }
        } catch (SQLException e) {
            throw new MyException(e, "DB2无法执行该查询列的SQL语句");
        } finally {
            //关闭相关资源
            JDBCUtils.shutDownDataBaseResource(rs, ps, connection, false);
        }
        return fieldMetaList;
    }

}
