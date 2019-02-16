"""
Prereqs: 
    - pip install seaborn

"""

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import os

sns.set(style="darkgrid")

df = pd.read_csv(os.getcwd() + "/data.csv")
sns.relplot(x="real_jaccard", y="jaccard_error", kind="line", data=df)
#plt.savefig("errors.png")
plt.show()
