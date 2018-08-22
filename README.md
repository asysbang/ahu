# ahu
安卓应用热更新


#演示说明



    <1>bug修复

        app作为基础版本，会除以0导致异常

        升级一个修复的补丁，安装补丁之后，会添加log同时解决除以0的异常

        注意

        ./intermediates/classes/debug/com/asysbang/androidhotupdate/demo/DevideByZero.class
        ./intermediates/classes/release/com/asysbang/androidhotupdate/demo/DevideByZero.class

        在生成dex的时候一定要看class文件是否是最新的，release目录的有时候不会更新，尽量用debug目录下的class

        生成dex的方法

        cp DevideByZero.class demo/com/asysbang/androidhotupdate/demo/

        dx --dex --output=classes2.dex demo   （注意这个demo是根目录）




    <2>增量更新

        app作为host，只有最基础的显示一个 TextView

        targetapp 作为target，实现一个按钮的点击效果

        实现一个补丁作为host到target的热升级


