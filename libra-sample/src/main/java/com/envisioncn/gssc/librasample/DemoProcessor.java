package com.envisioncn.gssc.librasample;

import org.springframework.batch.item.ItemProcessor;

/**
 * @author zhongshuangli
 * @date 2021-04-03
 */
public class DemoProcessor implements ItemProcessor<DemoEntity, DemoEntity> {
    @Override
    public DemoEntity process(DemoEntity item) throws Exception {
        item.setMessage(item.getMessage() + "_processed");
        return item;
    }
}
