<template>
  <div class="number-box">
    <div class="outbox"><div class="one">1</div></div>
    <div class="outbox"><div class="two">2</div></div>
    <div class="outbox"><div class="three">3</div></div>
    <div class="outbox"><div class="four">4</div></div>
  </div>
</template>

<script setup>

</script>

<style scoped>
.number-box {
    height: 100px;
    width: 220px;
    border-radius: 25px;
}
.outbox {
    display: inline-block;
    width: 40px;
    height: 85px;
    background-color: rgba(255, 255, 255, 0.2);
    border-radius: 10px;
    margin: 0px 7px;
}
.one, .two, .three, .four {
    display: inline-block;
    font-size: 55px;
    color: white;
    margin: 0px 6px;
    line-height: 85px;
}
</style>