package com.jansansad

import com.cibo.evilplot._
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme.defaultTheme

import java.awt.image.BufferedImage
import java.awt.Image
import javax.swing.{ImageIcon, JFrame, JLabel, WindowConstants}
import scala.collection.Seq

object PlotApp extends App {

  def showPlot(plot: com.cibo.evilplot.geometry.Drawable): Unit = {
    val originalImage: BufferedImage = plot.asBufferedImage

    val maxWidth  = 800
    val maxHeight = 600

    val originalWidth  = originalImage.getWidth
    val originalHeight = originalImage.getHeight

    val scale = Math.min(maxWidth.toDouble / originalWidth, maxHeight.toDouble / originalHeight)

    val newWidth  = (originalWidth * scale).toInt
    val newHeight = (originalHeight * scale).toInt

    val scaledImage: Image = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)

    val frame = new JFrame("JanSansad Chart")
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    frame.setSize(newWidth, newHeight)

    val icon  = new ImageIcon(scaledImage)
    val label = new JLabel(icon)
    frame.getContentPane.add(label)

    frame.pack()
    frame.setVisible(true)
  }

  object plotIt {
    def apply(data: Seq[Seq[Double]], relativeData: Seq[Seq[Double]], xAxisWords: Seq[String], labels: Seq[String]): Unit = {
      val word = xAxisWords.toList

      val title: String => String = arg => word.length match {
        case 1 => s"Jansansad $arg Bar Chart for ${word(0)}"
        case 2 => s"Jansansad $arg Bar Chart for ${word(0)} and ${word(1)}"
      }

      val barChart = BarChart
        .clustered(
          data.map(_.toList).toList,
          labels = labels.toList
        )
        .title(title("Absolute"))
        .xAxis(word)
        .yAxis()
        .frame()
        .bottomLegend()
        .render()

      val barChartRelative = BarChart
        .clustered(
          relativeData.map(_.toList).toList,
          labels = labels.toList
        )
        .title(title("Relative"))
        .xAxis(word)
        .yAxis()
        .frame()
        .bottomLegend()
        .render()

      showPlot(barChart)
      showPlot(barChartRelative)

      println("Press ENTER to exit...")
      scala.io.StdIn.readLine()

    }
  }

}