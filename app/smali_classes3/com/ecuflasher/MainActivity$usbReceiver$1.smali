.class public final Lcom/ecuflasher/MainActivity$usbReceiver$1;
.super Landroid/content/BroadcastReceiver;
.source "MainActivity.kt"


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/ecuflasher/MainActivity;-><init>()V
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x19
    name = null
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000\u001d\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000*\u0001\u0000\u0008\n\u0018\u00002\u00020\u0001J\u001c\u0010\u0002\u001a\u00020\u00032\u0008\u0010\u0004\u001a\u0004\u0018\u00010\u00052\u0008\u0010\u0006\u001a\u0004\u0018\u00010\u0007H\u0016\u00a8\u0006\u0008"
    }
    d2 = {
        "com/ecuflasher/MainActivity$usbReceiver$1",
        "Landroid/content/BroadcastReceiver;",
        "onReceive",
        "",
        "context",
        "Landroid/content/Context;",
        "intent",
        "Landroid/content/Intent;",
        "app_debug"
    }
    k = 0x1
    mv = {
        0x1,
        0x9,
        0x0
    }
    xi = 0x30
.end annotation


# instance fields
.field final synthetic this$0:Lcom/ecuflasher/MainActivity;


# direct methods
.method constructor <init>(Lcom/ecuflasher/MainActivity;)V
    .locals 0
    .param p1, "$receiver"    # Lcom/ecuflasher/MainActivity;

    iput-object p1, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    .line 93
    invoke-direct {p0}, Landroid/content/BroadcastReceiver;-><init>()V

    return-void
.end method


# virtual methods
.method public onReceive(Landroid/content/Context;Landroid/content/Intent;)V
    .locals 6
    .param p1, "context"    # Landroid/content/Context;
    .param p2, "intent"    # Landroid/content/Intent;

    .line 95
    const/4 v0, 0x0

    if-eqz p2, :cond_0

    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;

    move-result-object v1

    goto :goto_0

    :cond_0
    move-object v1, v0

    :goto_0
    iget-object v2, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    invoke-static {v2}, Lcom/ecuflasher/MainActivity;->access$getACTION_USB_PERMISSION$p(Lcom/ecuflasher/MainActivity;)Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z

    move-result v1

    if-nez v1, :cond_1

    return-void

    .line 97
    :cond_1
    const-string v1, "permission"

    const/4 v2, 0x0

    invoke-virtual {p2, v1, v2}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z

    move-result v1

    .line 98
    .local v1, "granted":Z
    const-string v2, "statusText"

    if-eqz v1, :cond_3

    .line 99
    sget-object v3, Lcom/ecuflasher/EcuLogger;->INSTANCE:Lcom/ecuflasher/EcuLogger;

    const-string v4, "USB permission granted"

    invoke-virtual {v3, v4}, Lcom/ecuflasher/EcuLogger;->usb(Ljava/lang/String;)V

    .line 100
    new-instance v3, Lcom/ecuflasher/UsbDeviceManager;

    iget-object v4, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    check-cast v4, Landroid/content/Context;

    invoke-direct {v3, v4}, Lcom/ecuflasher/UsbDeviceManager;-><init>(Landroid/content/Context;)V

    .line 101
    .local v3, "manager":Lcom/ecuflasher/UsbDeviceManager;
    invoke-virtual {v3}, Lcom/ecuflasher/UsbDeviceManager;->openTactrixChannel()Lcom/ecuflasher/TactrixTestResult;

    move-result-object v4

    .line 102
    .local v4, "result":Lcom/ecuflasher/TactrixTestResult;
    iget-object v5, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    invoke-static {v5}, Lcom/ecuflasher/MainActivity;->access$getStatusText$p(Lcom/ecuflasher/MainActivity;)Landroid/widget/TextView;

    move-result-object v5

    if-nez v5, :cond_2

    invoke-static {v2}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    goto :goto_1

    :cond_2
    move-object v0, v5

    :goto_1
    iget-object v2, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    invoke-static {v2, v4}, Lcom/ecuflasher/MainActivity;->access$buildStatusText(Lcom/ecuflasher/MainActivity;Lcom/ecuflasher/TactrixTestResult;)Ljava/lang/String;

    move-result-object v2

    check-cast v2, Ljava/lang/CharSequence;

    invoke-virtual {v0, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .end local v3    # "manager":Lcom/ecuflasher/UsbDeviceManager;
    .end local v4    # "result":Lcom/ecuflasher/TactrixTestResult;
    goto :goto_3

    .line 104
    :cond_3
    sget-object v3, Lcom/ecuflasher/EcuLogger;->INSTANCE:Lcom/ecuflasher/EcuLogger;

    const-string v4, "USB permission denied"

    invoke-virtual {v3, v4}, Lcom/ecuflasher/EcuLogger;->usb(Ljava/lang/String;)V

    .line 105
    iget-object v3, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    invoke-static {v3}, Lcom/ecuflasher/MainActivity;->access$getStatusText$p(Lcom/ecuflasher/MainActivity;)Landroid/widget/TextView;

    move-result-object v3

    if-nez v3, :cond_4

    invoke-static {v2}, Lkotlin/jvm/internal/Intrinsics;->throwUninitializedPropertyAccessException(Ljava/lang/String;)V

    goto :goto_2

    :cond_4
    move-object v0, v3

    :goto_2
    check-cast v4, Ljava/lang/CharSequence;

    invoke-virtual {v0, v4}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 108
    :goto_3
    iget-object v0, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    invoke-static {v0}, Lcom/ecuflasher/MainActivity;->access$refreshDeveloperLog(Lcom/ecuflasher/MainActivity;)V

    .line 109
    iget-object v0, p0, Lcom/ecuflasher/MainActivity$usbReceiver$1;->this$0:Lcom/ecuflasher/MainActivity;

    invoke-static {v0}, Lcom/ecuflasher/MainActivity;->access$refreshDebugPanel(Lcom/ecuflasher/MainActivity;)V

    .line 110
    return-void
.end method
