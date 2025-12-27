<script setup lang="ts">
import websocketService from "@/modules/platform/api/services/websocket.service.ts"
import { WebSocketOpCodes } from "@/modules/platform/api/models/websocket.response.ts"

const props = defineProps<{ instanceId: string }>()

websocketService.send(WebSocketOpCodes.InstanceLogsRequest, {
    iid: props.instanceId
})
</script>

<template>Pirocoptero 333</template>

<style scoped lang="scss"></style>
