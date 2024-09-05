package com.sonnet.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sonnet.model.domain.Tag;
import com.sonnet.service.TagService;
import com.sonnet.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author chang
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-09-02 11:38:45
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService{

}




