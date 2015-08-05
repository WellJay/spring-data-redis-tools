package com.brandbigdata.rep.business.action;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.brandbigdata.daemon.Rlinks;
import com.brandbigdata.rep.redis.impl.StringRedisDao;
import com.brandbigdata.utils.DateUtil;

@Controller
@RequestMapping("system")
public class SystemController extends SuperController {
	
	@Resource
	private StringRedisDao redis;

	@RequestMapping("test")
	public String test(){
		//redis对象储存方法
		Rlinks rlinks = new Rlinks();
		rlinks.setLine("456");
		try {
			redis.addObject("test", rlinks, DateUtil.getSecondFromDay(7), Rlinks.class);
		} catch (Exception e) {
			//log.error(e.getMessage());
		}
		Rlinks r1 = (Rlinks)redis.getObject("test");
		System.out.println(r1.getLine());
		
		return "system/files/addrenwu";
	}
}