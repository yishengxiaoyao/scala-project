# Git Reset、Rebase和Revert的区别
## Git Revert
git revert 之前的提交仍会保留在git log中，而此次撤销会做为一次新的提交。

## Git Reset
git reset 是回滚到某次提交
git reset --soft
此次提交之后的修改会被退回到暂存区
git reset --hard
此次提交之后的修改不做任何保留，git status干净的工作区。

## Git Rebase

git rebase 当两个分支不在一条直线上，需要执行merge操作时，使用该命令操作。

## 参考文献
[git reset 、rebase和 revert的区别](https://blog.csdn.net/rebeccachong/article/details/39379703)