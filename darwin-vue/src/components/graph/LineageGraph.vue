<script setup>
import go, { Routing, TextOverflow, Wrap } from 'gojs'
import { onUnmounted, shallowRef, watch } from 'vue'
import { asyncGetLineageChildren, asyncGetLineageNode } from '@/common/AsyncRequest'

const props = defineProps({ node: Object })
const diagram = shallowRef()

const findParent = async node => {
  if (!node.parent_key || node.parent) return
  const parent = await asyncGetLineageNode(node.parent_key)
  const parentNode = { key: parent.key, text: parent.url, parent_key: parent.parent_key, type: 'precursor' }
  parentNode.children = [node]
  node.parent = parentNode
  diagram.value.model.addNodeData(parentNode)
  diagram.value.model.addLinkData({ from: parentNode.key, to: node.key, progress: true })
}

const findChildren = async node => {
  if (node.children && node.children.length > 0) return
  const children = await asyncGetLineageChildren(node.key)
  const childNodes = []
  children.forEach(child => {
    const childNode = { key: child.key, text: child.url, parent_key: child.parent_key, type: 'subsequence' }
    childNode.parent = node
    diagram.value.model.addNodeData(childNode)
    diagram.value.model.addLinkData({ from: node.key, to: childNode.key, progress: true })
    childNodes.push(childNode)
  })
  node.children = childNodes
}

const destroyDiagram = () => {
  if (diagram.value) {
    diagram.value.clear()
    diagram.value = undefined
  }
}

const initDiagram = async () => {
  destroyDiagram()
  const colors = {
    pink: '#facbcb',
    blue: '#b7d8f7',
    green: '#b9e1c8',
    yellow: '#faeb98',
    background: '#e8e8e8'
  }
  const nodeTemplate = new go.Node('Auto', {
    isShadowed: true,
    shadowBlur: 0,
    shadowOffset: new go.Point(5, 5),
    shadowColor: 'gray',
    toolTip: go.GraphObject.build('ToolTip').add(
      new go.Panel('Vertical').add(new go.TextBlock({ margin: 8 })
        .bind('text'))),
    click: (e, node) => {
      findParent(node.data)
      findChildren(node.data)
    }})
    .bindTwoWay('location', 'loc', go.Point.parse, go.Point.stringify)
    .add(
      new go.Shape('RoundedRectangle', {
        strokeWidth: 1.5,
        fill: colors.blue,
        maxSize: new go.Size(350, 100),
        portId: '',
        fromLinkable: false, fromLinkableSelfNode: false, fromLinkableDuplicates: false,
        toLinkable: false, toLinkableSelfNode: false, toLinkableDuplicates: false,
        cursor: 'pointer'
      }).bind('fill', 'type', type => {
        if (type === 'precursor') return colors.green
        if (type === 'subsequence') return colors.yellow
        return colors.blue}),
      new go.TextBlock({
        shadowVisible: false,
        margin: 8,
        font: 'bold 14px sans-serif',
        stroke: '#333',
        alignment: go.Spot.LeftCenter,
        overflow: TextOverflow.Ellipsis,
        wrap: Wrap.None,
        editable: false
      }).bindTwoWay('text'))

  nodeTemplate.selectionAdornmentTemplate = new go.Adornment('Spot')
    .add(
      new go.Panel('Auto').add(
        new go.Shape('RoundedRectangle', { fill: null, stroke: colors.pink, strokeWidth: 3 }),
        new go.Placeholder()))

  const linkTemplate = new go.Link({
    isShadowed: true,
    shadowBlur: 0,
    shadowColor: 'gray',
    shadowOffset: new go.Point(2.5, 2.5),
    curve: go.Curve.JumpOver,
    curviness: 20,
    corner: 5,
    adjusting: go.LinkAdjusting.Stretch,
    reshapable: true,
    relinkableFrom: false,
    relinkableTo: false,
    fromShortLength: 8,
    toShortLength: 10,
    routing: Routing.AvoidsNodes
  })
    .bindTwoWay('points')
    .bind('curviness')
    .add(
      new go.Shape({ strokeWidth: 1.5, shadowVisible: false, stroke: 'black' })
        .bind('opacity', 'progress', (progress) => (progress ? 1 : 0.5)),
      new go.Shape({ fromArrow: 'circle', strokeWidth: 1.5, fill: 'white' }).
        bind('opacity', 'progress', (progress) => (progress ? 1 : 0.5)),
      new go.Shape({ toArrow: 'standard', stroke: null, scale: 1.5, fill: 'black' }).
        bind('opacity', 'progress', (progress) => (progress ? 1 : 0.5)))

  diagram.value = new go.Diagram('graph', {
    'undoManager.isEnabled': false,
    'toolManager.hoverDelay': 100,
    'animationManager.initialAnimationStyle': go.AnimationStyle.None,
    InitialAnimationStarting: (e) => {
      const animation = e.subject.defaultAnimation
      animation.easing = go.Animation.EaseOutExpo
      animation.duration = 800
      animation.add(e.diagram, 'scale', 0.3, 1)
      animation.add(e.diagram, 'opacity', 0, 1)
    },
    initialContentAlignment: go.Spot.Center,
    layout: new go.TreeLayout({
      angle: 0,
      layerSpacing: 100,
      nodeSpacing: 60,
      alignment: go.TreeLayout.AlignmentCenterChildren,
      sortDirection: go.TreeLayout.SortingForwards
    }),
    nodeTemplate: nodeTemplate,
    linkTemplate: linkTemplate
  })
  diagram.value.div.style.backgroundColor = colors.background
}

watch(() => props.node, () => {
  if (props.node) {
    initDiagram()
    diagram.value.model = new go.GraphLinksModel([props.node])
  }
})
onUnmounted(() => destroyDiagram())
</script>

<template>
  <div id="graph" class="graph-container"></div>
</template>

<style scoped>
.gojs-watermark { display: none !important; }
.graph-container {
  width: 100%;
  height: 500px;
  max-width: 1200px;
  overflow: hidden;
  margin: 0 auto;
}
</style>