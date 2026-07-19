"""临时脚本：从 DB 读取加密的 API key，AES 解密后推送到 RAG 服务。
用完即可删除。"""
import base64
import hashlib
import json
import mysql.connector
import httpx
from Crypto.Cipher import AES
from Crypto.Util.Padding import unpad

AES_KEY_RAW = "oa-v2-aes-key-2026"
AES_KEY = hashlib.md5(AES_KEY_RAW.encode()).hexdigest()[:16].encode()

DB = dict(host="localhost", user="root", password="123456", database="springboot_oa_v2")
RAG_URL = "http://localhost:8001/api/v1/config"

DEFAULT_SYSTEM_PROMPT = """你是 OA 系统的知识库问答助手。你的任务是基于下方 Context 中提供的文档内容，回答用户的提问。

请严格遵循以下规则：
1. 输出语言：始终使用简体中文回答。
2. 输出格式：使用规范的 Markdown，可用标题（#/##/###）、有序列表（1. 2.）、无序列表（-）、表格等。
3. 数字与金额：所有数字、年限、金额、天数必须使用阿拉伯数字（如 5 天、500 元、3 年、1.5 倍）。禁止用星号（*）、字母（a/o/x）或其他符号代替数字。
4. 章节编号：保持原文档章节顺序，如"第一章"、"1.1"、"2.3"。禁止写成"第章第二"这种错乱格式。
5. 内容准确性：严格基于 Context 中的内容回答，不要编造信息。Context 中提到的所有数字、年限、金额必须原样引用，不得修改。
6. 引用来源：不要在末尾添加 References 列表，除非用户明确要求。
7. 简洁性：回答简明扼要，避免重复。
8. 回答要求：必须直接回答用户问题。如果 Context 中包含相关内容，必须给出具体回答；只有当 Context 完全没有相关内容时，才能说明"知识库中暂无相关内容"。

---Context---
{context_data}"""


def decrypt(b64: str) -> str:
    if not b64:
        return ""
    cipher = AES.new(AES_KEY, AES.MODE_ECB)
    plain = unpad(cipher.decrypt(base64.b64decode(b64)), 16)
    return plain.decode()


def main():
    conn = mysql.connector.connect(**DB)
    cur = conn.cursor(dictionary=True)
    cur.execute("SELECT * FROM ai_config WHERE is_default=1 ORDER BY id LIMIT 1")
    row = cur.fetchone()
    # 始终用最新版默认提示词覆盖 DB（便于迭代调试）
    if row and row["system_prompt"] != DEFAULT_SYSTEM_PROMPT:
        cur.execute("UPDATE ai_config SET system_prompt=%s WHERE id=%s",
                    (DEFAULT_SYSTEM_PROMPT, row["id"]))
        conn.commit()
        row["system_prompt"] = DEFAULT_SYSTEM_PROMPT
        print(f"已更新 DB 中的 system_prompt (id={row['id']})")
    conn.close()
    if not row:
        print("ERROR: no default config")
        return

    cfg = {
        "embedding_provider": row["embedding_provider"],
        "embedding_base_url": row["embedding_base_url"],
        "embedding_model": row["embedding_model"],
        "embedding_api_key": decrypt(row["embedding_api_key"]),
        "embedding_dim": row["embedding_dim"],
        "llm_provider": row["llm_provider"],
        "llm_base_url": row["llm_base_url"],
        "llm_model": row["llm_model"],
        "llm_api_key": decrypt(row["llm_api_key"]),
        "llm_temperature": float(row["llm_temperature"]),
        "llm_max_tokens": row["llm_max_tokens"],
        "system_prompt": row["system_prompt"] or DEFAULT_SYSTEM_PROMPT,
        "reranker_provider": row["reranker_provider"],
        "reranker_base_url": row["reranker_base_url"],
        "reranker_model": row["reranker_model"],
        "reranker_api_key": decrypt(row["reranker_api_key"]),
        "mock_mode": row["mock_mode"],
    }
    print(f"Pushing config: mock_mode={cfg['mock_mode']}, llm={cfg['llm_model']}, emb={cfg['embedding_model']}")
    print(f"  llm_key len={len(cfg['llm_api_key'])}, emb_key len={len(cfg['embedding_api_key'])}")
    print(f"  system_prompt len={len(cfg['system_prompt'])}")
    r = httpx.post(RAG_URL, json=cfg, timeout=10)
    print(f"RAG response: {r.status_code} {r.text}")


if __name__ == "__main__":
    main()
