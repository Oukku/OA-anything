package com.jlwl.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("kb_document")
public class KbDocumentEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    @TableField("kb_id")
    private Long kbId;
    @TableField("filename")
    private String filename;
    @TableField("storage_path")
    private String storagePath;
    @TableField("rag_doc_id")
    private String ragDocId;
    @TableField("file_size")
    private Long fileSize;
    @TableField("file_type")
    private String fileType;
    @TableField("status")
    private Integer status;
    @TableField("error_msg")
    private String errorMsg;
    @TableLogic
    @TableField("del_flag")
    private Integer delFlag;
    @TableField("create_by")
    private Long createBy;
    @TableField("create_time")
    private LocalDateTime createTime;
    @TableField("update_time")
    private LocalDateTime updateTime;
}
