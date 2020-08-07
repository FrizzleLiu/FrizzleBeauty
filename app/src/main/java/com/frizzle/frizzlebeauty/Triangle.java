package com.frizzle.frizzlebeauty;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

/**
 * author: LWJ
 * date: 2020/7/29$
 * description
 * 绘制三角形
 * 初始化和渲染都在这里做
 * 也就是顶点着色器和片元着色器的相关操作
 */
public class Triangle {

    //三角形的顶点
    static float triangleCoords[] = {
            0.5f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f
    };

    private int mProgram;
    private FloatBuffer vertexBuffer;
    private float colors[] = {1.0f,1.0f,1.0f,1.0f};
    //顶点着色器代码
    private String vertexShaderCode =  "attribute vec4 vPosition;" +
            "uniform mat4 vMatrix;" +
            "void main() {" +
            "  gl_Position = vMatrix*vPosition;" +
            "}";

    //片元着色器代码
    private String fragmentShaderCode = "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";

    private float [] mViewMatrix = new float[16];
    private float [] mProjectMatrix = new float[16];
    private float [] mMVPMatrix = new float[16];

    public void onSurfaceChanged(GL10 gl10,int width ,int height){
        //计算宽高比
        float ratio = (float)width / height;
        //投影矩阵
        Matrix.frustumM(mProjectMatrix,0,-ratio,ratio,-1,1,3,120);
        //相机矩阵
        Matrix.setLookAtM(mViewMatrix,0,0,0,7,//摄像机的坐标
                0f,0f,0f,//目标物的坐标
                0f,1f,0f);//相机方向
        //矩阵计算变换
        //将相机矩阵和投影矩阵相与 将结果放在mMVPMatrix
        Matrix.multiplyMM(mMVPMatrix,0,mProjectMatrix,0,mViewMatrix,0);
    }

    /**
     * 构造方法中初始化
     */
    public Triangle() {
        //ByteBuffer相当于使用gpu的桥梁
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(triangleCoords.length * 4);//float占4个字节
        //gpu内存排列顺序,使用默认
        byteBuffer.order(ByteOrder.nativeOrder());
        //转化为管道
        vertexBuffer = byteBuffer.asFloatBuffer();
        //将语法推送给GPU
        vertexBuffer.put(triangleCoords);
        vertexBuffer.position(0);

        //创建顶点着色器
        int shader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        //编译
        GLES20.glShaderSource(shader,vertexShaderCode);
        GLES20.glCompileShader(shader);

        //创建片元着色器
        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        //编译
        GLES20.glShaderSource(fragmentShader,fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader);

        //将顶点着色器和片元着色器交给统一程序管理
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram,shader);
        GLES20.glAttachShader(mProgram,fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    /**
     * @param gl10
     * 渲染
     */
    public void onDrawFrame(GL10 gl10) {
        GLES20.glUseProgram(mProgram);
        int mMatrixHandler = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        GLES20.glUniformMatrix4fv(mMatrixHandler,1,false,mMVPMatrix,0);

        //渲染顶点
        //相当于一个指针 指向GPU中 vPosition的地址
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram,"vPosition");
        //启用读写
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle,3,GLES20.GL_FLOAT,false,3*4,vertexBuffer);


        //渲染颜色
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        GLES20.glUniform4fv(mColorHandle,1,colors,0);

        //调用绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0,3);

        //禁用读写
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
