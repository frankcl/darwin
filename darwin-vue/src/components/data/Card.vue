<script setup>
const props = defineProps({
  title: { required: false },
  'paddingTop': { default: 30 },
  'paddingBottom': { default: 30 },
  'paddingLeft': { default: 30 },
  'paddingRight': { default: 30 },
})
</script>

<template>
  <div>
    <div class="card-title">
      <slot name="title">{{ props.title }}</slot>
    </div>
    <div class="card">
      <div class="card-body" :style="{ padding: paddingTop + 'px ' +
            paddingRight + 'px ' + paddingBottom + 'px ' + paddingLeft + 'px'}">
        <slot></slot>
      </div>
    </div>
  </div>
</template>

<style scoped>
.card {
  --bs-card-box-shadow: rgba(145, 158, 171, 0.2) 0px 0px 2px 0px, rgba(145, 158, 171, 0.12) 0px 12px 24px -4px;
  position: relative;
  display: -webkit-box;
  display: -ms-flexbox;
  display: flex;
  -webkit-box-orient: vertical;
  -webkit-box-direction: normal;
  -ms-flex-direction: column;
  flex-direction: column;
  min-width: 0;
  word-wrap: break-word;
  background-color: #fff;
  background-clip: border-box;
  border: 0 solid #ebf1f6;
  border-radius: 7px;
  -webkit-box-shadow: var(--bs-card-box-shadow);
  box-shadow: var(--bs-card-box-shadow);
  margin-bottom: 30px;
}
.card-title {
  color: #2A3547;
  font-size: 16px;
  font-weight: 600;
  padding-left: 10px;
  margin-bottom: 0.8rem;
}
.card-body {
  -webkit-box-flex: 1;
  -ms-flex: 1 1 auto;
  flex: 1 1 auto;
  padding: 30px 30px;
}
</style>