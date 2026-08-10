# Ollama AI 模型下載與設定指南

本指南說明如何在已經啟動的 `api-assist-ollama` 容器中，下載並執行對「繁體中文」支援度高的大型語言模型 (LLM)。

## 推薦模型 (繁體中文友善)

1. **Qwen 2.5 (通義千問 2.5)** - 執行代號：`qwen2.5` (7B 參數)
   - **特色**：目前開源界在中文（含繁體）理解與生成表現最頂尖的模型之一。非常聰明、推論速度快，是目前的**首選推薦**。
2. **Llama 3.1** - 執行代號：`llama3.1` (8B 參數)
   - **特色**：Meta 最新開源模型，綜合邏輯能力極強，只要在提示詞 (Prompt) 中要求「請使用繁體中文」，就能有極佳的表現。
3. **Breeze (達哥)** - 執行代號：`ycchen/breeze-7b-instruct-v1_0`
   - **特色**：由台灣聯發科 (MediaTek Research) 釋出，是特別針對台灣繁體中文語境與在地文化進行微調的模型。

## 向量化模型 (Embedding Models)

由於您有使用到 RAG 架構與 `pgvector`，如果您打算讓本地的 Ollama 負責將文字轉換成向量 (Embeddings)，您還需要下載專門的向量模型：
1. **Nomic Embed Text** - 執行代號：`nomic-embed-text`
   - **特色**：非常輕量且高效的開源向量模型，專門用來計算語義相似度。

## 下載步驟 

目前我已經在背景為您自動下載 **Qwen 2.5** 模型。若您未來需要手動下載其他模型，請依照以下步驟操作：

1. 打開終端機 (Terminal / PowerShell)。
2. 執行以下指令，透過 Docker 進入 ollama 容器並拉取 (pull) 模型：
   ```bash
   docker exec api-assist-ollama ollama pull qwen2.5
   ```
   *(若要下載其他模型，只需將 `qwen2.5` 替換為 `llama3.1` 等名稱即可)*
3. 等待下載完成 (7B 級別的模型約需下載 4.7 GB 左右的檔案，時間取決於網速)。
4. 驗證模型是否下載成功，您可以列出目前已有的模型清單：
   ```bash
   docker exec api-assist-ollama ollama list
   ```

## 如何直接測試對話

下載完成後，您可以直接在終端機中進入互動模式，測試它的繁體中文能力：
```bash
docker exec -it api-assist-ollama ollama run qwen2.5
```
進入後直接輸入中文發問即可。*(測試完畢後，輸入 `/bye` 即可退出對話)*

---
*💡 備註：所有下載的模型資料，都會透過 Docker Volume 持久化儲存在您專案目錄下的 `ollama_data` 資料夾中。因此即使您把容器刪除或重啟，下次都不需要重新下載！*
