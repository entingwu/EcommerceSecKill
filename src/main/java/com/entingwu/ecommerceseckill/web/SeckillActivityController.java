package com.entingwu.ecommerceseckill.web;

import com.entingwu.ecommerceseckill.db.dao.SeckillActivityDao;
import com.entingwu.ecommerceseckill.db.dao.SeckillCommodityDao;
import com.entingwu.ecommerceseckill.db.dao.SeckillOrderDao;
import com.entingwu.ecommerceseckill.db.po.SeckillActivity;
import com.entingwu.ecommerceseckill.db.po.SeckillCommodity;
import com.entingwu.ecommerceseckill.db.po.SeckillOrder;
import com.entingwu.ecommerceseckill.service.SeckillActivityService;
import com.entingwu.ecommerceseckill.util.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class SeckillActivityController {

    @Autowired
    private SeckillActivityDao seckillActivityDao;
    @Autowired
    private SeckillCommodityDao seckillCommodityDao;

    @Autowired
    private SeckillOrderDao seckillOrderDao;

    @Autowired
    private SeckillActivityService seckillActivityService;

    @Resource
    private RedisService redisService;

    @RequestMapping("/seckills")
    public String activityList(Map<String, Object> resultMap) {
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
        resultMap.put("seckillActivities", seckillActivities);
        return "seckill_activity";
    }

    /**
     * @param resultMap
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/item/{seckillActivityId}")
    public String itemPage(Map<String, Object> resultMap, @PathVariable long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());

        resultMap.put("seckillActivity",seckillActivity);
        resultMap.put("seckillCommodity",seckillCommodity);
        resultMap.put("seckillPrice",seckillActivity.getSeckillPrice());
        resultMap.put("oldPrice",seckillActivity.getOldPrice());
        resultMap.put("commodityId",seckillActivity.getCommodityId());
        resultMap.put("commodityName",seckillCommodity.getCommodityName());
        resultMap.put("commodityDesc",seckillCommodity.getCommodityDesc());
        return "seckill_item";
    }

    // @ResponseBody
    @RequestMapping("/addSeckillActivityAction")
    public String addSeckillActivityAction(
            @RequestParam("name") String name,
            @RequestParam("commodityId") long commodityId,
            @RequestParam("seckillPrice") BigDecimal seckillPrice,
            @RequestParam("oldPrice") BigDecimal oldPrice,
            @RequestParam("seckillNumber") long seckillNumber,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime") String endTime
            ) throws ParseException {
        startTime = startTime.substring(0, 10) + startTime.substring(11);
        endTime = endTime.substring(0, 10) + endTime.substring(11);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddhh:mm");
        SeckillActivity seckillActivity = new SeckillActivity();
        seckillActivity.setName(name);
        seckillActivity.setCommodityId(commodityId);
        seckillActivity.setSeckillPrice(seckillPrice);
        seckillActivity.setOldPrice(oldPrice);
        seckillActivity.setTotalStock(seckillNumber);
        seckillActivity.setAvailableStock(new Integer("" + seckillNumber));
        seckillActivity.setLockStock(0L);
        seckillActivity.setActivityStatus(1);
        seckillActivity.setStartTime(format.parse(startTime));
        seckillActivity.setEndTime(format.parse(endTime));
        seckillActivityDao.insertSeckillActivity(seckillActivity);
        return seckillActivity.toString();
    }

    @RequestMapping("/addSeckillActivity")
    public String addSeckillActivity() {
        return "add_activity";
    }

    /**
     * Seckill request
     * @param userId
     * @param seckillActivityId
     * @return
     */
    @RequestMapping("/seckill/buy/{userId}/{seckillActivityId}")
    public ModelAndView seckillCommodity(
            @PathVariable long userId,
            @PathVariable long seckillActivityId
    ) {
        boolean stockValidateResult = false;
        ModelAndView modelAndView = new ModelAndView();
        try {
            // 1. check if user is in purchased list
            if (redisService.isInLimitMember(seckillActivityId, userId)) {
                modelAndView.addObject("resultInfo", "Sorry, you are in the limit purchase list");
                modelAndView.setViewName("seckill_result");
                return modelAndView;
            }

            // 2. check if it can seckill
            stockValidateResult = seckillActivityService.seckillStockValiator(seckillActivityId);
            if (stockValidateResult) {
                SeckillOrder order = seckillActivityService.createOrder(seckillActivityId, userId);
                modelAndView.addObject("resultInfo", "Seckill successfully, the order is creating, order Id: " + order.getOrderNo());
                modelAndView.addObject("orderNo", order.getOrderNo());
                // 3. add user to limit purchase list
                redisService.addLimitMember(seckillActivityId, userId);
            } else {
                modelAndView.addObject("resultInfo", "Sorry, the inventory is not enough");
            }
        } catch (Exception e) {
            log.error("Exception in seckill activity: ", e.toString());
            modelAndView.addObject("resultInfo", "Seckill failure");
        }
        modelAndView.setViewName("seckill_result");
        return modelAndView;
    }

    // Display Order
    @RequestMapping("/seckill/orderQuery/{orderNo}")
    public ModelAndView orderQuery(
            @PathVariable String orderNo
    ) {
        log.info("Order query, orderNo: " + orderNo);
        SeckillOrder order = seckillOrderDao.querySeckillOrder(orderNo);
        ModelAndView modelAndView = new ModelAndView();

        if (order != null) {
            modelAndView.setViewName("order");
            modelAndView.addObject("order", order);
            SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(order.getSeckillActivityId());
            modelAndView.addObject("seckillActivity", seckillActivity);
        } else {
            modelAndView.setViewName("order_wait");
        }
        return modelAndView;
    }

    @RequestMapping("/seckill/payOrder/{orderNo}")
    public String payOrder(
            @PathVariable String orderNo
    ) throws Exception {
        seckillActivityService.payOrderProcess(orderNo);
        return "redirect:/seckill/orderQuery/" + orderNo;
    }
}
