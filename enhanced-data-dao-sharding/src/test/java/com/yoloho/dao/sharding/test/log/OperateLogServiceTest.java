package com.yoloho.dao.sharding.test.log;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.yoloho.dao.sharding.impl.ShardedDaoImpl;
import com.yoloho.dao.sharding.support.util.ShardingFactorUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:context.xml")
public class OperateLogServiceTest {

	@Autowired
	ShardedDaoImpl<OperateLog, Long> operateLogShardedDao;
	
	@Test
	public void testInsert() {
		try {
			OperateLog entity = new OperateLog();
			entity.setId(1000l);
			entity.setCreated(new Date());
			operateLogShardedDao.replace(entity);

			OperateLog entity2 = new OperateLog();
			entity2.setId(1001l);
			entity2.setCreated(new Date());
			operateLogShardedDao.replace(entity2);
		}catch(Exception exp) {
			exp.printStackTrace();
		}
	}
	
	@Test
	public void testQuery() {
		try {
			OperateLog entity = operateLogShardedDao.get("id", 1005, ShardingFactorUtils.build("created", new Date()));
			System.out.println(entity);
		}catch(Exception exp) {
			exp.printStackTrace();
		}
	}
	
}