package com.maigen.common.mybatis.util;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

/**
 * MyBatis工具类
 */
public class MyBatisUtil {

    /**
     * 获取SqlSession
     */
    public static SqlSession getSqlSession(SqlSessionFactory sqlSessionFactory) {
        return sqlSessionFactory.openSession(true);
    }

    /**
     * 关闭SqlSession
     */
    public static void closeSqlSession(SqlSession sqlSession) {
        if (sqlSession != null) {
            sqlSession.close();
        }
    }
}