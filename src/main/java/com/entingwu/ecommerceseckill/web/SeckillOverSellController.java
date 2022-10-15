package com.entingwu.ecommerceseckill.web;

import com.entingwu.ecommerceseckill.service.SeckillActivityService;
import com.entingwu.ecommerceseckill.service.SeckillOverSellService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SeckillOverSellController {

    @Autowired
    private SeckillOverSellService seckillOverSellService;

    @Autowired
    private SeckillActivityService seckillActivityService;

    /**
     * 1. process seckill request
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    // @RequestMapping("/seckill/{seckillActivityId}")
    public String seckill(@PathVariable long seckillActivityId) {
        return seckillOverSellService.processSeckill(seckillActivityId);
    }

    /**
     * 2. use lua script to process seckill request
     * @param seckillActivityId
     * @return
     */
    @ResponseBody
    @RequestMapping("/seckill/{seckillActivityId}")
    public String seckillCommodity(@PathVariable long seckillActivityId) {
        boolean stockValidateResult = seckillActivityService.seckillStockValiator(seckillActivityId);
        return stockValidateResult ? "Congratulation! Thanks for purchase." : "The product is sold out. Good luck next time.";
    }
}
