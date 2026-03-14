.class public final Lcom/ecuflasher/UsbDeviceManager;
.super Ljava/lang/Object;
.source "UsbDeviceManager.kt"


# annotations
.annotation system Ldalvik/annotation/SourceDebugExtension;
    value = "SMAP\nUsbDeviceManager.kt\nKotlin\n*S Kotlin\n*F\n+ 1 UsbDeviceManager.kt\ncom/ecuflasher/UsbDeviceManager\n+ 2 _Collections.kt\nkotlin/collections/CollectionsKt___CollectionsKt\n*L\n1#1,86:1\n288#2,2:87\n*S KotlinDebug\n*F\n+ 1 UsbDeviceManager.kt\ncom/ecuflasher/UsbDeviceManager\n*L\n23#1:87,2\n*E\n"
.end annotation

.annotation runtime Lkotlin/Metadata;
    d1 = {
        "\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0008\u0002\n\u0002\u0010\u0008\n\u0002\u0008\u0004\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\n\u001a\u00020\u000bR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0008\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0006X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000c"
    }
    d2 = {
        "Lcom/ecuflasher/UsbDeviceManager;",
        "",
        "context",
        "Landroid/content/Context;",
        "(Landroid/content/Context;)V",
        "READ_TIMEOUT_MS",
        "",
        "TACTRIX_PRODUCT_ID",
        "TACTRIX_VENDOR_ID",
        "WRITE_TIMEOUT_MS",
        "openTactrixChannel",
        "Lcom/ecuflasher/TactrixTestResult;",
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
.field private final READ_TIMEOUT_MS:I

.field private final TACTRIX_PRODUCT_ID:I

.field private final TACTRIX_VENDOR_ID:I

.field private final WRITE_TIMEOUT_MS:I

.field private final context:Landroid/content/Context;


# direct methods
.method public constructor <init>(Landroid/content/Context;)V
    .locals 1
    .param p1, "context"    # Landroid/content/Context;

    const-string v0, "context"

    invoke-static {p1, v0}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullParameter(Ljava/lang/Object;Ljava/lang/String;)V

    .line 11
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    iput-object p1, p0, Lcom/ecuflasher/UsbDeviceManager;->context:Landroid/content/Context;

    .line 13
    const/16 v0, 0x403

    iput v0, p0, Lcom/ecuflasher/UsbDeviceManager;->TACTRIX_VENDOR_ID:I

    .line 14
    const v0, 0xcc4d

    iput v0, p0, Lcom/ecuflasher/UsbDeviceManager;->TACTRIX_PRODUCT_ID:I

    .line 15
    const/16 v0, 0xfa0

    iput v0, p0, Lcom/ecuflasher/UsbDeviceManager;->READ_TIMEOUT_MS:I

    .line 16
    const/16 v0, 0xbb8

    iput v0, p0, Lcom/ecuflasher/UsbDeviceManager;->WRITE_TIMEOUT_MS:I

    .line 11
    return-void
.end method


# virtual methods
.method public final openTactrixChannel()Lcom/ecuflasher/TactrixTestResult;
    .locals 14

    .line 20
    iget-object v0, p0, Lcom/ecuflasher/UsbDeviceManager;->context:Landroid/content/Context;

    const-string v1, "usb"

    invoke-virtual {v0, v1}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    const-string v1, "null cannot be cast to non-null type android.hardware.usb.UsbManager"

    invoke-static {v0, v1}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;Ljava/lang/String;)V

    check-cast v0, Landroid/hardware/usb/UsbManager;

    .line 23
    .local v0, "usbManager":Landroid/hardware/usb/UsbManager;
    invoke-virtual {v0}, Landroid/hardware/usb/UsbManager;->getDeviceList()Ljava/util/HashMap;

    move-result-object v1

    invoke-virtual {v1}, Ljava/util/HashMap;->values()Ljava/util/Collection;

    move-result-object v1

    const-string v2, "<get-values>(...)"

    invoke-static {v1, v2}, Lkotlin/jvm/internal/Intrinsics;->checkNotNullExpressionValue(Ljava/lang/Object;Ljava/lang/String;)V

    check-cast v1, Ljava/lang/Iterable;

    .local v1, "$this$firstOrNull$iv":Ljava/lang/Iterable;
    const/4 v2, 0x0

    .line 87
    .local v2, "$i$f$firstOrNull":I
    invoke-interface {v1}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;

    move-result-object v3

    :cond_0
    invoke-interface {v3}, Ljava/util/Iterator;->hasNext()Z

    move-result v4

    const/4 v5, 0x1

    const/4 v6, 0x0

    if-eqz v4, :cond_2

    invoke-interface {v3}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v4

    .local v4, "element$iv":Ljava/lang/Object;
    move-object v7, v4

    check-cast v7, Landroid/hardware/usb/UsbDevice;

    .local v7, "it":Landroid/hardware/usb/UsbDevice;
    const/4 v8, 0x0

    .line 24
    .local v8, "$i$a$-firstOrNull-UsbDeviceManager$openTactrixChannel$tactrixDevice$1":I
    invoke-virtual {v7}, Landroid/hardware/usb/UsbDevice;->getVendorId()I

    move-result v9

    iget v10, p0, Lcom/ecuflasher/UsbDeviceManager;->TACTRIX_VENDOR_ID:I

    if-ne v9, v10, :cond_1

    .line 25
    invoke-virtual {v7}, Landroid/hardware/usb/UsbDevice;->getProductId()I

    move-result v9

    iget v10, p0, Lcom/ecuflasher/UsbDeviceManager;->TACTRIX_PRODUCT_ID:I

    if-ne v9, v10, :cond_1

    move v9, v5

    goto :goto_0

    :cond_1
    move v9, v6

    .line 24
    :goto_0
    nop

    .line 87
    .end local v7    # "it":Landroid/hardware/usb/UsbDevice;
    .end local v8    # "$i$a$-firstOrNull-UsbDeviceManager$openTactrixChannel$tactrixDevice$1":I
    if-eqz v9, :cond_0

    goto :goto_1

    .line 88
    .end local v4    # "element$iv":Ljava/lang/Object;
    :cond_2
    const/4 v4, 0x0

    .line 23
    .end local v1    # "$this$firstOrNull$iv":Ljava/lang/Iterable;
    .end local v2    # "$i$f$firstOrNull":I
    :goto_1
    check-cast v4, Landroid/hardware/usb/UsbDevice;

    .line 22
    move-object v1, v4

    .line 28
    .local v1, "tactrixDevice":Landroid/hardware/usb/UsbDevice;
    if-nez v1, :cond_3

    .line 29
    new-instance v2, Lcom/ecuflasher/TactrixTestResult;

    const-string v3, "No Tactrix device detected"

    invoke-direct {v2, v6, v3}, Lcom/ecuflasher/TactrixTestResult;-><init>(ZLjava/lang/String;)V

    return-object v2

    .line 32
    :cond_3
    invoke-virtual {v0, v1}, Landroid/hardware/usb/UsbManager;->openDevice(Landroid/hardware/usb/UsbDevice;)Landroid/hardware/usb/UsbDeviceConnection;

    move-result-object v2

    .line 34
    .local v2, "connection":Landroid/hardware/usb/UsbDeviceConnection;
    if-nez v2, :cond_4

    .line 35
    new-instance v3, Lcom/ecuflasher/TactrixTestResult;

    .line 36
    nop

    .line 37
    nop

    .line 35
    const-string v4, "Failed to open Tactrix USB device (permission?)"

    invoke-direct {v3, v6, v4}, Lcom/ecuflasher/TactrixTestResult;-><init>(ZLjava/lang/String;)V

    return-object v3

    .line 41
    :cond_4
    invoke-virtual {v1, v6}, Landroid/hardware/usb/UsbDevice;->getInterface(I)Landroid/hardware/usb/UsbInterface;

    move-result-object v3

    .line 43
    .local v3, "usbInterface":Landroid/hardware/usb/UsbInterface;
    if-nez v3, :cond_5

    .line 44
    invoke-virtual {v2}, Landroid/hardware/usb/UsbDeviceConnection;->close()V

    .line 45
    new-instance v4, Lcom/ecuflasher/TactrixTestResult;

    const-string v5, "No USB interface found"

    invoke-direct {v4, v6, v5}, Lcom/ecuflasher/TactrixTestResult;-><init>(ZLjava/lang/String;)V

    return-object v4

    .line 48
    :cond_5
    invoke-virtual {v2, v3, v5}, Landroid/hardware/usb/UsbDeviceConnection;->claimInterface(Landroid/hardware/usb/UsbInterface;Z)Z

    move-result v4

    .line 50
    .local v4, "claimed":Z
    if-nez v4, :cond_6

    .line 51
    invoke-virtual {v2}, Landroid/hardware/usb/UsbDeviceConnection;->close()V

    .line 52
    new-instance v5, Lcom/ecuflasher/TactrixTestResult;

    const-string v7, "Failed to claim interface"

    invoke-direct {v5, v6, v7}, Lcom/ecuflasher/TactrixTestResult;-><init>(ZLjava/lang/String;)V

    return-object v5

    .line 55
    :cond_6
    const/4 v7, 0x0

    .line 56
    .local v7, "endpointOut":Landroid/hardware/usb/UsbEndpoint;
    const/4 v8, 0x0

    .line 58
    .local v8, "endpointIn":Landroid/hardware/usb/UsbEndpoint;
    const/4 v9, 0x0

    .local v9, "i":I
    invoke-virtual {v3}, Landroid/hardware/usb/UsbInterface;->getEndpointCount()I

    move-result v10

    :goto_2
    if-ge v9, v10, :cond_9

    .line 60
    invoke-virtual {v3, v9}, Landroid/hardware/usb/UsbInterface;->getEndpoint(I)Landroid/hardware/usb/UsbEndpoint;

    move-result-object v11

    .line 62
    .local v11, "endpoint":Landroid/hardware/usb/UsbEndpoint;
    invoke-virtual {v11}, Landroid/hardware/usb/UsbEndpoint;->getDirection()I

    move-result v12

    if-nez v12, :cond_7

    .line 63
    move-object v7, v11

    .line 66
    :cond_7
    invoke-virtual {v11}, Landroid/hardware/usb/UsbEndpoint;->getDirection()I

    move-result v12

    const/16 v13, 0x80

    if-ne v12, v13, :cond_8

    .line 67
    move-object v8, v11

    .line 58
    .end local v11    # "endpoint":Landroid/hardware/usb/UsbEndpoint;
    :cond_8
    add-int/lit8 v9, v9, 0x1

    goto :goto_2

    .line 71
    .end local v9    # "i":I
    :cond_9
    if-eqz v7, :cond_b

    if-nez v8, :cond_a

    goto :goto_3

    .line 77
    :cond_a
    invoke-virtual {v2, v3}, Landroid/hardware/usb/UsbDeviceConnection;->releaseInterface(Landroid/hardware/usb/UsbInterface;)Z

    .line 78
    invoke-virtual {v2}, Landroid/hardware/usb/UsbDeviceConnection;->close()V

    .line 80
    new-instance v6, Lcom/ecuflasher/TactrixTestResult;

    .line 81
    nop

    .line 82
    nop

    .line 80
    const-string v9, "Tactrix USB interface opened successfully"

    invoke-direct {v6, v5, v9}, Lcom/ecuflasher/TactrixTestResult;-><init>(ZLjava/lang/String;)V

    return-object v6

    .line 72
    :cond_b
    :goto_3
    invoke-virtual {v2, v3}, Landroid/hardware/usb/UsbDeviceConnection;->releaseInterface(Landroid/hardware/usb/UsbInterface;)Z

    .line 73
    invoke-virtual {v2}, Landroid/hardware/usb/UsbDeviceConnection;->close()V

    .line 74
    new-instance v5, Lcom/ecuflasher/TactrixTestResult;

    const-string v9, "Bulk endpoints not found"

    invoke-direct {v5, v6, v9}, Lcom/ecuflasher/TactrixTestResult;-><init>(ZLjava/lang/String;)V

    return-object v5
.end method
