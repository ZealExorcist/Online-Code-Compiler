<template>
  <div class="snippet">
    <CompilerInterface :snippet-id="id" />
  </div>
</template>

<script>
import CompilerInterface from '../components/CompilerInterface.vue'

export default {
  name: 'Snippet',
  components: {
    CompilerInterface
  },
  props: {
    id: {
      type: String,
      required: true
    }
  }
}
</script>
