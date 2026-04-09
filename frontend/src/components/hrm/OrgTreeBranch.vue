<script setup>
defineOptions({
  name: 'OrgTreeBranch',
})

const props = defineProps({
  node: {
    type: Object,
    required: true,
  },
})

const emit = defineEmits(['select'])

function handleSelect() {
  emit('select', props.node)
}

function forwardSelect(node) {
  emit('select', node)
}
</script>

<template>
  <div class="org-branch" :class="{ 'org-branch--root': node.isRoot }">
    <button
      type="button"
      class="org-branch__card text-left"
      :class="[
        node.isRoot ? 'org-branch__card--root' : '',
        node.status !== 'ACTIVE' ? 'org-branch__card--muted' : '',
      ]"
      @click="handleSelect"
    >
      <div class="org-branch__eyebrow">
        {{ node.typeLabel }}
      </div>
      <div class="org-branch__title">
        {{ node.name }}
      </div>
      <div class="org-branch__meta">
        {{ node.managerName }}
      </div>
      <div class="org-branch__footer">
        <span class="org-branch__badge">
          {{ node.totalEmployeeCount }} nhân sự
        </span>
        <span class="org-branch__hint">
          {{ node.childCount ? `${node.childCount} đơn vị con` : 'Mở danh sách' }}
        </span>
      </div>
    </button>

    <div v-if="node.children?.length" class="org-branch__children-wrap">
      <div class="org-branch__vertical-line"></div>
      <div class="org-branch__children-line"></div>
      <div class="org-branch__children">
        <div
          v-for="child in node.children"
          :key="child.id"
          class="org-branch__child"
        >
          <div class="org-branch__child-line"></div>
          <OrgTreeBranch :node="child" @select="forwardSelect" />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.org-branch {
  display: flex;
  min-width: max-content;
  flex-direction: column;
  align-items: center;
}

.org-branch__card {
  display: flex;
  min-height: 148px;
  width: 260px;
  flex-direction: column;
  justify-content: space-between;
  border-radius: 24px;
  border: 1px solid rgba(148, 163, 184, 0.22);
  background: linear-gradient(180deg, #ffffff, #f8fafc);
  padding: 16px;
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.2s ease;
  cursor: pointer;
}

.org-branch__card:hover {
  transform: translateY(-3px);
  border-color: rgba(99, 102, 241, 0.3);
  box-shadow: 0 24px 56px rgba(79, 70, 229, 0.16);
}

.org-branch__card--root {
  border-color: rgba(99, 102, 241, 0.45);
  background: linear-gradient(135deg, #0f172a, #312e81 52%, #4f46e5);
  color: white;
}

.org-branch__card--muted {
  border-color: rgba(244, 63, 94, 0.2);
}

.org-branch__eyebrow {
  font-size: 10px;
  font-weight: 900;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: #64748b;
}

.org-branch__card--root .org-branch__eyebrow {
  color: rgba(224, 231, 255, 0.82);
}

.org-branch__title {
  margin-top: 0.55rem;
  font-size: 18px;
  font-weight: 900;
  line-height: 1.25;
  color: #0f172a;
}

.org-branch__card--root .org-branch__title {
  color: white;
}

.org-branch__meta {
  margin-top: 0.35rem;
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}

.org-branch__card--root .org-branch__meta {
  color: rgba(224, 231, 255, 0.82);
}

.org-branch__footer {
  margin-top: 1rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.org-branch__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  background: rgba(79, 70, 229, 0.08);
  padding: 0.45rem 0.85rem;
  font-size: 12px;
  font-weight: 900;
  color: #4338ca;
}

.org-branch__card--root .org-branch__badge {
  background: rgba(255, 255, 255, 0.14);
  color: #e0e7ff;
}

.org-branch__hint {
  font-size: 12px;
  font-weight: 700;
  color: #64748b;
}

.org-branch__card--root .org-branch__hint {
  color: rgba(224, 231, 255, 0.82);
}

.org-branch__children-wrap {
  position: relative;
  display: flex;
  width: 100%;
  flex-direction: column;
  align-items: center;
  padding-top: 28px;
}

.org-branch__vertical-line {
  position: absolute;
  top: 0;
  height: 16px;
  width: 2px;
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.55), rgba(148, 163, 184, 0.22));
}

.org-branch__children-line {
  position: absolute;
  top: 16px;
  left: 50%;
  height: 2px;
  width: calc(100% - 160px);
  min-width: 0;
  transform: translateX(-50%);
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.15), rgba(99, 102, 241, 0.55), rgba(99, 102, 241, 0.15));
}

.org-branch__children {
  display: flex;
  align-items: flex-start;
  justify-content: center;
  gap: 28px;
  width: 100%;
}

.org-branch__child {
  position: relative;
  display: flex;
  min-width: max-content;
  flex-direction: column;
  align-items: center;
}

.org-branch__child-line {
  height: 16px;
  width: 2px;
  background: linear-gradient(180deg, rgba(99, 102, 241, 0.55), rgba(148, 163, 184, 0.22));
}

@media (max-width: 767px) {
  .org-branch__card {
    width: 220px;
    min-height: 136px;
    padding: 14px;
  }

  .org-branch__title {
    font-size: 16px;
  }

  .org-branch__children {
    gap: 16px;
  }

  .org-branch__children-line {
    width: calc(100% - 120px);
  }
}
</style>
