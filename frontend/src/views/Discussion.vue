<template>
  <div class="discussion-page">
    <div class="page-header">
      <div>
        <p class="subtitle">课程讨论区</p>
        <h2>最新话题与互动</h2>
      </div>
      <el-button type="primary" round>
        <el-icon><ChatDotRound /></el-icon>
        发起话题
      </el-button>
    </div>

    <div class="topic-list">
      <div v-for="topic in topics" :key="topic.id" class="topic-card">
        <div class="topic-head">
          <div class="avatar">{{ topic.author[0] }}</div>
          <div>
            <p class="author">{{ topic.author }}</p>
            <small>{{ topic.time }}</small>
          </div>
          <el-tag size="small" round>{{ topic.tag }}</el-tag>
        </div>
        <h3>{{ topic.title }}</h3>
        <p class="content">{{ topic.preview }}</p>
        <div class="topic-footer">
          <span>{{ topic.replies }} 条回复</span>
          <span>{{ topic.participants }} 人参与</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ChatDotRound } from '@element-plus/icons-vue'
import { ElButton, ElIcon, ElTag } from 'element-plus'

defineOptions({
  name: 'DiscussionView'
})

const topics = [
  {
    id: 1,
    author: '王晓明',
    time: '2 小时前',
    title: '如何拆分复杂的用户故事？',
    preview: '我们在迭代计划时遇到一个完整功能拆分的问题，欢迎分享你们的实践经验……',
    replies: 12,
    participants: 8,
    tag: '需求讨论'
  },
  {
    id: 2,
    author: '李若彤',
    time: '昨天 21:08',
    title: '风险登记表模板分享',
    preview: '附上上周整理的风险登记表，包含触发条件、应对策略，有需要的组可以直接使用……',
    replies: 6,
    participants: 5,
    tag: '资料分享'
  },
  {
    id: 3,
    author: '赵江',
    time: '昨天 15:20',
    title: '迭代回顾如何引导队友积极发言',
    preview: '我们组回顾时讨论气氛不够热烈，想听听大家在主持会议时的提问技巧和注意事项……',
    replies: 9,
    participants: 7,
    tag: '团队协作'
  }
]
</script>

<style scoped>
.discussion-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.subtitle {
  margin: 0;
  color: #9ca3af;
}

.topic-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.topic-card {
  background: #fff;
  border-radius: 24px;
  padding: 24px;
  box-shadow: 0 20px 35px rgba(15, 23, 42, 0.08);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.topic-head {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 16px;
  background: #1f2937;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}

.author {
  margin: 0;
  font-weight: 600;
}

.topic-card h3 {
  margin: 0;
  font-size: 18px;
}

.content {
  margin: 0;
  color: #6b7280;
}

.topic-footer {
  display: flex;
  gap: 24px;
  color: #9ca3af;
  font-size: 13px;
}
</style>

