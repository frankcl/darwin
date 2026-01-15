<script setup>
import go, { Routing, TextOverflow, Wrap } from 'gojs'
import { onUnmounted, shallowRef, watch } from 'vue'
import { writeClipboard } from '@/common/Clipboard'
import { showMessage, SUCCESS } from '@/common/Feedback'
import { asyncGetLineageChildren, asyncGetLineageNode } from '@/common/AsyncRequest'

go.Shape.defineFigureGenerator('Document', (shape, w, h) => {
  const geo = new go.Geometry()
  h = h / 0.8
  const fig = new go.PathFigure(0, 0.7 * h, true)
  geo.add(fig)
  fig.add(new go.PathSegment(go.SegmentType.Line, 0, 0))
  fig.add(new go.PathSegment(go.SegmentType.Line, w, 0))
  fig.add(new go.PathSegment(go.SegmentType.Line, w, 0.7 * h))
  fig.add(new go.PathSegment(go.SegmentType.Bezier, 0, 0.7 * h, 0.5 * w, 0.4 * h, 0.5 * w, h).close())
  geo.spot1 = go.Spot.TopLeft
  geo.spot2 = new go.Spot(1, 0.6)
  return geo
})

go.Shape.defineFigureGenerator('MultiDocument', (shape, w, h) => {
  const geo = new go.Geometry()
  h = h / 0.8
  const fig = new go.PathFigure(w, 0, true)
  geo.add(fig)
  // Outline
  fig.add(new go.PathSegment(go.SegmentType.Line, w, 0.5 * h))
  fig.add(new go.PathSegment(go.SegmentType.Bezier, 0.9 * w, 0.44 * h, 0.96 * w, 0.47 * h, 0.93 * w, 0.45 * h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0.9 * w, 0.6 * h))
  fig.add(new go.PathSegment(go.SegmentType.Bezier, 0.8 * w, 0.54 * h, 0.86 * w, 0.57 * h, 0.83 * w, 0.55 * h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0.8 * w, 0.7 * h))
  fig.add(new go.PathSegment(go.SegmentType.Bezier, 0, 0.7 * h, 0.4 * w, 0.4 * h, 0.4 * w, h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0, 0.2 * h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0.1 * w, 0.2 * h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0.1 * w, 0.1 * h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0.2 * w, 0.1 * h))
  fig.add(new go.PathSegment(go.SegmentType.Line, 0.2 * w, 0).close())
  const fig2 = new go.PathFigure(0.1 * w, 0.2 * h, false)
  geo.add(fig2)
  // Inside lines
  fig2.add(new go.PathSegment(go.SegmentType.Line, 0.8 * w, 0.2 * h))
  fig2.add(new go.PathSegment(go.SegmentType.Line, 0.8 * w, 0.54 * h))
  fig2.add(new go.PathSegment(go.SegmentType.Move, 0.2 * w, 0.1 * h))
  fig2.add(new go.PathSegment(go.SegmentType.Line, 0.9 * w, 0.1 * h))
  fig2.add(new go.PathSegment(go.SegmentType.Line, 0.9 * w, 0.44 * h))
  geo.spot1 = new go.Spot(0, 0.25)
  geo.spot2 = new go.Spot(0.8, 0.77)
  return geo
})

const props = defineProps({ node: Object })
const diagram = shallowRef()

const copyURL = async btn => {
  if (btn.elements.count > 0) btn.removeAt(btn.elements.count - 1)
  btn.add(new go.Shape('MultiDocument', { width: 14, height: 14, fill: null }))
  const node = btn.part.adornedPart.data
  await writeClipboard(node.text)
  showMessage('复制链接成功', SUCCESS)
}

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
        new go.Placeholder()
      ),
      go.GraphObject.build('Button', {
        alignment: go.Spot.TopRight,
        '_buttonFillOver': 'transparent',
        '_buttonFillNormal': 'transparent',
        '_buttonFillPressed': 'transparent',
        '_buttonStrokeWidth': 0,
        '_buttonStrokeOver': null,
        '_buttonStrokeNormal': null,
        '_buttonStrokePressed': null,
        click: (e, btn) => copyURL(btn),
        toolTip: go.GraphObject.build('ToolTip').add(new go.TextBlock({ margin: 8, text: '点击复制' }))
      }).add(
        new go.Shape('Document', { width: 14, height: 14, fill: null }))
    )

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